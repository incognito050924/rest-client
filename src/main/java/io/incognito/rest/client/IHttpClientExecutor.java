package io.incognito.rest.client;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.HashMap;

import io.incognito.rest.client.handler.HttpCallbackHandler;
import io.incognito.rest.client.helper.ClientResponseProcessor;
import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.dto.response.IBaseResponse;
import io.incognito.rest.client.util.MapUtil;
import io.incognito.rest.client.util.Opt;
import reactor.core.publisher.Mono;

public interface IHttpClientExecutor<AUTH> extends IHttpClient<AUTH> {

    <S extends WebClient.RequestHeadersSpec<?>> void authorize(S builder, AUTH auth);

    /////////////////////////////////////////////////////
    //////// Declarations:  Non-Blocking Methods ////////
    /////////////////////////////////////////////////////

    // Start Declarations: Request with Request Body and Content-Type //
    /**
     * 요청 파라미터를 사용하여 비동기 요청을 수행합니다.
     * URL_ENCODED_FORM_DATA 요청은 별도 메서드 지원 안 하므로 해당 메서드 사용하면 됨
     *
     * @param request 요청 파라미터 객체
     * @param contentType 요청 컨텐츠 타입 (기본값: {@link MediaType#APPLICATION_FORM_URLENCODED}) **Either {@link MediaType#APPLICATION_FORM_URLENCODED} or {@link MediaType#MULTIPART_FORM_DATA}
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyInserterAsync(final BodyInserter<REQ, ? super ClientHttpRequest> request, final MediaType contentType, final Class<RESP> responseType, final Integer retryCount, final HttpCallbackHandler<RESP> handler) {
        final Mono<RESP> respMono = authorizedBuilder(getAuthorization())
                .headers(headers -> Opt.of(contentType).ifPresent(headers::setContentType))
                .body(request)
                .exchangeToMono(clientResponse -> ClientResponseProcessor.handleResponse(clientResponse, responseType, retryCount));

        return ClientResponseProcessor.applyProcessErrorResumeAndSetCallbackHandler(responseType, handler).apply(respMono);
    }

    /**
     * 요청 파라미터를 사용하여 비동기 요청을 수행합니다.
     * URL_ENCODED_FORM_DATA 요청은 별도 메서드 지원 안 하므로 해당 메서드 사용하면 됨
     *
     * @param request 요청 파라미터 객체
     * @param contentType 요청 컨텐츠 타입 (기본값: {@link MediaType#APPLICATION_FORM_URLENCODED}) **Either {@link MediaType#APPLICATION_FORM_URLENCODED} or {@link MediaType#MULTIPART_FORM_DATA}
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyInserterAsync(final BodyInserter<REQ, ? super ClientHttpRequest> request, final MediaType contentType, final Class<RESP> responseType, final Integer retryCount) {
        return executeWithBodyInserterAsync(request, contentType, responseType, retryCount, null);
    }
    // End Declarations: Request with Request Body and Content-Type //

    // Start Declarations: Request with JSON body //
    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다.
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType, final Integer retryCount, final HttpCallbackHandler<RESP> handler) {
        return executeWithBodyInserterAsync(BodyInserters.fromValue(request), null, responseType, retryCount, handler);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다. (재시도 횟수: 0)
     *
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        return executeWithBodyAsync(request, responseType, null, handler);
    }

/**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다.
     *
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType, final Integer retryCount) {
        return executeWithBodyAsync(request, responseType, retryCount, null);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다. (재시도 횟수: 0)
     *
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType) {
        return executeWithBodyAsync(request, responseType, null, null);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다.
     *
     * @param request 요청 파라미터 객체
     * @param retryCount 최대 재시도 횟수
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request, final Integer retryCount, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithBodyAsync(request, EmptyOrStringBodyResponse.class, retryCount, handler);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     *
     * @param request 요청 파라미터 객체
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithBodyAsync(request, EmptyOrStringBodyResponse.class, null, handler);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다.
     *
     * @param request 요청 파라미터 객체
     * @param retryCount 최대 재시도 횟수
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request, final Integer retryCount) {
        return executeWithBodyAsync(request, retryCount, null);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     *
     * @param request 요청 파라미터 객체
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request) {
        return executeWithBodyAsync(request, 0, null);
    }
    // End Declarations: Request with JSON Body //

    // Start Declarations: Request with FormData or URL Encoded FormData //
    /**
     * Multipart FormData 를 사용하여 비동기 요청을 수행합니다.
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param handler 라이프사이클 핸들러
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Class<RESP> responseType, final Integer retryCount, final HttpCallbackHandler<RESP> handler) {
        return executeWithBodyInserterAsync(BodyInserters.fromMultipartData(formDataBuilder.build()), MediaType.MULTIPART_FORM_DATA, responseType, retryCount, handler);
    }

    /**
     * Multipart FormData 를 사용하여 비동기 요청을 수행합니다.
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Class<RESP> responseType, final Integer retryCount) {
        return executeWithFormDataAsync(formDataBuilder, responseType, retryCount, null);
    }

    /**
     * Multipart FormData 를 사용하여 비동기 요청을 수행합니다. (재시도 횟수: 0)
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Class<RESP> responseType) {
        return executeWithFormDataAsync(formDataBuilder, responseType, 0, null);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다.
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param retryCount 최대 재시도 횟수
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Integer retryCount, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithFormDataAsync(formDataBuilder, EmptyOrStringBodyResponse.class, retryCount, handler);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithFormDataAsync(formDataBuilder, EmptyOrStringBodyResponse.class, 0, handler);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param retryCount 최대 재시도 횟수
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Integer retryCount) {
        return executeWithFormDataAsync(formDataBuilder, retryCount, null);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder) {
        return executeWithFormDataAsync(formDataBuilder, 0, null);
    }
    // End Declarations: Request with FormData or URL Encoded FormData //

    // Start Declarations: Request with no request parameter //
    /**
     * 파라미터 없이 비동기 요청을 수행합니다.
     *
     * @param responseType 응답 객체의 클래스 객체
     * @param retryCount 최대 재시도 횟수
     * @param handler 라이프사이클 핸들러
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeAsync(final Class<RESP> responseType, final Integer retryCount, final HttpCallbackHandler<RESP> handler) {
        final Mono<RESP> respMono = authorizedBuilder(getAuthorization())
                .exchangeToMono(clientResponse -> ClientResponseProcessor.handleResponse(clientResponse, responseType, retryCount));

        return ClientResponseProcessor.applyProcessErrorResumeAndSetCallbackHandler(responseType, handler).apply(respMono);
    }

    /**
     * 파라미터 없이 비동기 요청을 수행합니다. (재시도 횟수: 0)
     *
     * @param responseType 응답 객체의 클래스 객체
     * @param handler 라이프사이클 핸들러
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeAsync(final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        return executeAsync(responseType, null, handler);
    }

    /**
     * 파라미터 없이 비동기 요청을 수행합니다. (재시도 횟수: 0)
     *
     * @param responseType 응답 객체의 클래스 객체
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    default <RESP extends IBaseResponse> Mono<RESP> executeAsync(final Class<RESP> responseType) {
        return executeAsync(responseType, null);
    }

    /**
     * EmptyResponse 로 응답을 받는 비동기 요청을 수행합니다.
     *
     * @param retryCount 최대 재시도 횟수
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeAsync(final Integer retryCount, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeAsync(EmptyOrStringBodyResponse.class, retryCount, handler);
    }

    /**
     * EmptyResponse 로 응답을 받는 비동기 요청을 수행합니다. (재시도 횟수: 0)
     * @param handler 라이프사이클 핸들러
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeAsync(final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeAsync(EmptyOrStringBodyResponse.class, handler);
    }

    /**
     * EmptyResponse 로 응답을 받는 비동기 요청을 수행합니다. (재시도 횟수: 0)
     * @return 응답 객체의 Mono
     */
    default Mono<EmptyOrStringBodyResponse> executeAsync() {
        return executeAsync(EmptyOrStringBodyResponse.class, null);
    }
    // End Declarations: Request with no request parameter //

    ///////////////////////////////////////////////
    //////// Declarations: private methods ////////
    ///////////////////////////////////////////////

    /**
     * WebClient 빌더를 생성한다.
     *
     * @param auth 인증 정보
     * @return WebClient 빌더
     */
    default WebClient.RequestBodySpec authorizedBuilder(final AUTH auth) {
        final WebClient.RequestBodySpec builder = getWebClient().method(getMethod())
                .uri(uriBuilder -> uriBuilder.path(getUrl())
                        .queryParams(Opt.of(getQueryParam()).orElse(MapUtil.convertMultiValueMap(new HashMap<>())))
                        .build(Opt.of(getPathVariables()).orElse(Collections.emptyMap())))
                .headers(headers -> headers.putAll(Opt.of(getRequestHeaders()).orElse(MapUtil.convertMultiValueMap(new HashMap<>()))));
        authorize(builder, auth);
        return builder;
    }
}
