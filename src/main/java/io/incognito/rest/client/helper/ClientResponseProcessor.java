package io.incognito.rest.client.helper;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import io.incognito.rest.client.exceptions.ApiFailureException;
import io.incognito.rest.client.handler.HttpCallbackHandler;
import io.incognito.rest.client.types.dto.ApiResult;
import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.dto.response.IBaseResponse;
import io.incognito.rest.client.types.enums.ApiResultCode;
import io.incognito.rest.client.util.Opt;
import io.incognito.rest.client.util.TypeUtil;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class ClientResponseProcessor {
    /**
     * HTTP 상태를 기반으로 API 결과 객체를 생성한다.
     *
     * @param status HTTP 상태 코드
     * @return API 결과 객체
     */
    public static ApiResult setupApiResult(final HttpStatus status) {
        return ApiResult.builder()
                .status(status)
                .resultCode(ApiResultCode.fromHttpStatus(status))
                .build();
    }

    /**
     * API 실패 결과 객체를 생성한다.
     *
     * @param resultCode API 결과 코드
     * @param throwable 예외 객체 (실패를 야기한 예외 객체)
     * @param getFailureMessage 예외 객체로부터 실패 메시지를 추출(생성)하는 함수
     * @param getDetailMessage 예외 객체로부터 상세 메시지를 추출(생성)하는 함수
     * @return API 실패 결과 객체
     */
    public static ApiResult setupApiResult(final ApiResultCode resultCode, final Throwable throwable, final Function<? super Throwable, String> getFailureMessage, final Function<? super Throwable, String> getDetailMessage) {
        return ApiResult.builder()
                .resultCode(Opt.of(resultCode).orElse(ApiResultCode.INVALID_ETC))
                .failureMessage(Opt.of(getFailureMessage).flatMap(msgFn -> Opt.of(throwable).map(msgFn)).orElse(null))
                .failureDetail(Opt.of(getDetailMessage).flatMap(msgFn -> Opt.of(throwable).map(msgFn)).orElse(null))
                .build();
    }

    /**
     * RESP 타입의 객체를 생성한다.
     *
     * @param responseType 생성할 타입의 클래스 객체
     * @param <RESP> 생성할 타입
     * @return 생성된 객체
     */
    public static <RESP extends IBaseResponse> Mono<RESP> createResponseInstance(final Class<RESP> responseType, final HttpStatus status) {
        try {
            return Mono.just(Opt.of(responseType).get().newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            return Mono.error(new ApiFailureException(deserializeFailure(status, e.getMessage()), e.getMessage(), e));
        }
    }

    /**
     * Http Response 의 Body 를 RESP 타입의 객체로 변환한다.
     *
     * @param responseType 변환할 타입의 클래스 객체
     * @param <RESP> 변환할 타입
     * @return 객체 타입 변환 함수
     */
    public static <RESP extends IBaseResponse> Function<ClientResponse, ? extends Mono<RESP>> exchangeResponse(final Class<RESP> responseType) {
        return clientResponse -> {
            final HttpStatus statusCode = clientResponse.statusCode();
            if (clientResponse.statusCode().is4xxClientError() || clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .switchIfEmpty(Mono.just(""))
                        .flatMap(body -> {
                            final ApiResult result = setupApiResult(statusCode);
                            result.setFailureDetail(Opt.of(body).filter(StringUtils::hasText).orElse(null));
                            result.setFailureMessage(String.format("Failed to call API. Status Code: [%d] %s", statusCode.value(), statusCode.getReasonPhrase()));
                            return Mono.error(new ApiFailureException(result));
                        });
            } else {
                if (TypeUtil.isAssignableTypeOf(responseType, EmptyOrStringBodyResponse.class)) {
                    return clientResponse.bodyToMono(String.class)
                            .switchIfEmpty(Mono.just(""))
                            .flatMap(bodyString -> {
                                try {
                                    return createResponseInstance(EmptyOrStringBodyResponse.class, statusCode).map(emptyOrStringBodyResponse -> {
                                        emptyOrStringBodyResponse.setBodyString(bodyString);
                                        return responseType.cast(emptyOrStringBodyResponse);
                                    });
                                } catch (final Exception e) {
                                    return Mono.error(new ApiFailureException(deserializeFailure(statusCode, bodyString), e.getMessage(), e));
                                }
                            });
                }
                return clientResponse.bodyToMono(responseType)
                        .switchIfEmpty(Mono.defer(() -> {
                            try {
                                return Mono.just(responseType.newInstance());
                            } catch (final Exception e) {
                                return Mono.error(new ApiFailureException(deserializeFailure(statusCode, e.getMessage()), e.getMessage(), e));
                            }
                        }));
            }
        };
    }

    /**
     * ClientResponse 를 처리하여 RESP 타입의 객체로 변환한다.
     * - EmptyResponse 처리
     * - ApiResult 설정
     * - Retry 수행
     * - 예외 처리 (HttpStatus 오류, timeout, 기타 예외)
     *
     * @param clientResponse ClientResponse 객체
     * @param responseType 변환할 타입의 클래스 객체
     * @param retryCount 최대 재시도 횟수 (null 또는 0 이하의 정수 값일 때는 재시도 하지 않음)
     * @param <RESP> 변환할 타입
     * @return 변환된 RESP 객체 Mono
     */
    public static <RESP extends IBaseResponse> Mono<RESP> handleResponse(final ClientResponse clientResponse, final Class<RESP> responseType, final Integer retryCount) {
        final HttpStatus status = clientResponse.statusCode();
        return exchangeResponse(responseType).apply(clientResponse)
                .switchIfEmpty(createResponseInstance(responseType, status))
                .doOnNext(response -> {
                    if (response.getApiResult() == null || response.getApiResult().getResultCode() == null) {
                        response.setApiResult(setupApiResult(clientResponse.statusCode()));
                    }
                })
                // Retry
                .retryWhen(Retry.backoff(
                        Opt.of(retryCount).filter(i -> i > 0).orElse(0),
                        Duration.ofSeconds(1)).onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> findApiFailureException(retrySignal.failure()).orElseGet(() -> {
                                final String message = "Retry exhausted after " + retrySignal.totalRetriesInARow() + " retries.";
                                final ApiResult failureResult = setupApiResult(ApiResultCode.EXHAUSTED_RETIRES, retrySignal.failure(), err -> message, Throwable::getMessage);
                                return new ApiFailureException(failureResult, message, retrySignal.failure());
                            }))))
                .onErrorResume(ApiFailureException.class, throwable -> createResponseInstance(responseType, status).map(responseInstance -> {
                    responseInstance.setApiResult(throwable.getFailureResult());
                    return responseInstance;
                }));
    }

    /**
     * processErrorResumeAndSetCallbackHandler 메서드에 partial application 적용 (ClientResponse 를 처리 중 발생한 예외 처리기를 등록한다.)
     *
     * @param responseType 변환할 타입의 클래스 객체
     * @param handler HTTP 응답 Callback Handler
     * @param <RESP> Response 타입
     * @return 예외 처리 로직이 추가된 Response Mono 변환 함수
     */
    public static <RESP extends IBaseResponse> Function<Mono<RESP>, Mono<RESP>> applyProcessErrorResumeAndSetCallbackHandler(final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        return responseMono -> processErrorResumeAndSetCallbackHandler(responseMono, responseType, handler);
    }

    /**
     * ClientResponse 를 처리 중 발생한 예외 처리기를 등록한다.
     *
     * @param exchanged Response Mono
     * @param responseType 변환할 타입의 클래스 객체
     * @param handler HTTP 응답 Callback Handler
     * @param <RESP> Response 타입
     * @return 예외 처리 로직이 추가된 Response Mono
     */
    public static <RESP extends IBaseResponse> Mono<RESP> processErrorResumeAndSetCallbackHandler(final Mono<RESP> exchanged, final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        final Opt<HttpCallbackHandler<RESP>> handlerOpt = Opt.of(handler);
        final HttpStatus status = HttpStatus.BAD_GATEWAY;
        try {
            return exchanged
                    // Timeout Exception Handling
                    .onErrorResume(ReadTimeoutException.class, throwable -> createResponseInstance(responseType, status)
                            .map(responseInstance -> {
                                responseInstance.setApiResult(setupApiResult(ApiResultCode.CONNECTION_TIMEOUT, throwable, err -> "Request Timeout", Throwable::getMessage));
                                return responseInstance;
                            }))
                    // Timeout Exception Handling
                    .onErrorResume(SslHandshakeTimeoutException.class, throwable -> createResponseInstance(responseType, status)
                            .map(responseInstance -> {
                                responseInstance.setApiResult(setupApiResult(ApiResultCode.CONNECTION_FAIL, throwable, err -> "SSL Handshake Timeout", Throwable::getMessage));
                                return responseInstance;
                            }))
                    // Timeout Exception Handling
                    .onErrorResume(WebClientRequestException.class, throwable -> createResponseInstance(responseType, status)
                            .map(responseInstance -> {
                                responseInstance.setApiResult(setupApiResult(ApiResultCode.INVALID_NETWORK, throwable, err -> "Failed to connect to the server", Throwable::getMessage));
                                return responseInstance;
                            }))
                    // Fallback Exception Handling
                    .onErrorResume(throwable -> !findApiFailureException(throwable).isPresent(), throwable -> createResponseInstance(responseType, HttpStatus.INTERNAL_SERVER_ERROR)
                            .map(responseInstance -> {
                                responseInstance.setApiResult(setupApiResult(ApiResultCode.INVALID_SYSTEM, throwable, Throwable::getMessage, err -> {
                                    // Failure Detail 값 생성
                                    return Opt.of(err.getCause())
                                            .map(Throwable::getMessage)
                                            .map(msg -> String.format("Cause: %s", msg))
                                            .orElse(null);
                                }));
                                return responseInstance;
                            }))
                    .doOnSuccess(resp -> handlerOpt.ifPresent(handle -> handle.onResponse(resp)))
                    .doOnError(err -> handlerOpt.ifPresent(handle -> handle.onError(err)))
                    .doFinally(signal -> handlerOpt.ifPresent(handle -> handle.afterFinished(signal)));
        } catch (final Exception e) {
            handlerOpt.ifPresent(handle -> handle.onError(e));
            throw e;
        }
}

    static Optional<ApiFailureException> findApiFailureException(final Throwable throwable) {
        if (throwable instanceof ApiFailureException) {
            return Optional.of((ApiFailureException) throwable);
        } else if (throwable.getCause() != null) {
            return findApiFailureException(throwable.getCause());
        }
        return Optional.empty();
    }

    static ApiResult deserializeFailure(final HttpStatus status, final String detailMessage) {
        return ApiResult.builder()
                .resultCode(ApiResultCode.FAILED_TO_DESERIALIZE)
                .status(status)
                .failureMessage(String.format("Given HttpStatus %s. But, Failed to instantiation with default constructor.", status))
                .failureDetail(detailMessage)
                .build();
    }
}
