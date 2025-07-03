package io.incognito.rest.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.incognito.rest.client.handler.HttpCallbackHandler;
import io.incognito.rest.client.helper.ClientResponseProcessor;
import io.incognito.rest.client.types.dto.ClientContextImpl;
import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.dto.response.IBaseResponse;
import io.incognito.rest.client.util.MultiValueMaps;
import io.incognito.rest.client.util.Opt;
import lombok.AccessLevel;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
public abstract class HttpClientExecutors<AUTH> implements IHttpRequest<AUTH> {
    private final WebClient webClient;
    private final HttpMethod method;
    private final String url;
    private final MultiValueMap<String, String> requestHeaders;
    private final MultiValueMap<String, String> queryParam;
    private final Map<String, String> pathVariables;
    private final AUTH authorization;
    private final AuthorizationApplier<AUTH> authorizationApplier;
    protected final int retryCount;
    protected final Duration retryDelay;
    @Getter(AccessLevel.PROTECTED)
    protected final ClientContextImpl<IHttpRequest<AUTH>> context;

    protected HttpClientExecutors(WebClient webClient, HttpMethod method, String url, MultiValueMap<String, String> requestHeaders, MultiValueMap<String, String> queryParam, Map<String, String> pathVariables, AUTH authorization, AuthorizationApplier<AUTH> authorizationApplier, int retryCount, Duration retryDelay) {
        this.webClient = webClient;
        this.method = method;
        this.url = url;
        this.requestHeaders = requestHeaders;
        this.queryParam = queryParam;
        this.pathVariables = pathVariables;
        this.authorization = authorization;
        this.authorizationApplier = authorizationApplier;
        this.retryCount = Math.max(retryCount, 0); // 기본 재시도 횟수
        this.retryDelay = Opt.of(retryDelay).filter(delay -> delay.toMillis() >= 300).orElse(Duration.ofSeconds(1)); // 기본 재시도 지연 시간
        this.context = new ClientContextImpl<>(this);
    }

    @Override
    public <S extends WebClient.RequestHeadersSpec<?>> void authorize(S builder, AUTH auth) {
        Opt.of(getAuthorizationApplier())
                .ifPresent(applier -> applier.apply(builder, auth));
    }

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
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyInserterAsync(final BodyInserter<REQ, ? super ClientHttpRequest> request, final MediaType contentType, final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        // 요청 stream을 생성하고 요청 파라미터를 설정
        final Mono<RESP> respMono = authorizedBuilder(getAuthorization())
                .headers(headers -> Opt.of(contentType).ifPresent(headers::setContentType))
                .body(request)
                .httpRequest(context::setHttpRequest)
                .exchangeToMono(clientResponse -> ClientResponseProcessor.handleResponse(clientResponse, responseType, getRetryCount(), getRetryDelay()));

        return ClientResponseProcessor.applyProcessErrorResumeAndSetCallbackHandler(responseType, handler, context).apply(respMono);
    }

    /**
     * 요청 파라미터를 사용하여 비동기 요청을 수행합니다.
     * URL_ENCODED_FORM_DATA 요청은 별도 메서드 지원 안 하므로 해당 메서드 사용하면 됨
     *
     * @param request 요청 파라미터 객체
     * @param contentType 요청 컨텐츠 타입 (기본값: {@link MediaType#APPLICATION_FORM_URLENCODED}) **Either {@link MediaType#APPLICATION_FORM_URLENCODED} or {@link MediaType#MULTIPART_FORM_DATA}
     * @param responseType 응답 객체의 클래스 객체
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyInserterAsync(final BodyInserter<REQ, ? super ClientHttpRequest> request, final MediaType contentType, final Class<RESP> responseType) {
        return executeWithBodyInserterAsync(request, contentType, responseType, null);
    }
    // End Declarations: Request with Request Body and Content-Type //

    // Start Declarations: Request with JSON body //
    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다.
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        return executeWithBodyInserterAsync(BodyInserters.fromValue(request), null, responseType, handler);
    }

    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행합니다.
     *
     * @param request 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param <REQ> 요청 파라미터 객체의 타입
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ, RESP extends IBaseResponse> Mono<RESP> executeWithBodyAsync(final REQ request, final Class<RESP> responseType) {
        return executeWithBodyAsync(request, responseType, null);
    }


    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다.
     *
     * @param request 요청 파라미터 객체
     * @param handler 라이프사이클 핸들러
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request,  final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithBodyAsync(request, EmptyOrStringBodyResponse.class, handler);
    }


    /**
     * Request Body(JSON) 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     *
     * @param request 요청 파라미터 객체
     * @param <REQ> 요청 파라미터 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <REQ> Mono<EmptyOrStringBodyResponse> executeWithBodyAsync(final REQ request) {
        return executeWithBodyAsync(request, EmptyOrStringBodyResponse.class, null);
    }
    // End Declarations: Request with JSON Body //

    // Start Declarations: Request with FormData or URL Encoded FormData //
    /**
     * Multipart FormData 를 사용하여 비동기 요청을 수행합니다.
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @param responseType 응답 객체의 클래스 객체
     * @param handler 라이프사이클 핸들러
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <RESP extends IBaseResponse> Mono<RESP> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        final BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(formDataBuilder.build());
        return executeWithBodyInserterAsync(multipartInserter, MediaType.MULTIPART_FORM_DATA, responseType, handler);
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
    public <RESP extends IBaseResponse> Mono<RESP> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final Class<RESP> responseType) {
        return executeWithFormDataAsync(formDataBuilder, responseType, null);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다.
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @return 응답 객체의 Mono
     */
    public Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder, final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeWithFormDataAsync(formDataBuilder, EmptyOrStringBodyResponse.class, handler);
    }

    /**
     * Form Data 또는 URL Encoded Form 파라미터로 비동기 요청을 수행하고 응답을 {@link EmptyOrStringBodyResponse}로 받습니다. (재시도 횟수: 0)
     * @see io.incognito.rest.client.types.dto.request.MultipartFormDataRequest
     *
     * @param formDataBuilder 요청 파라미터 객체
     * @return 응답 객체의 Mono
     */
    public Mono<EmptyOrStringBodyResponse> executeWithFormDataAsync(final MultipartBodyBuilder formDataBuilder) {
        return executeWithFormDataAsync(formDataBuilder, EmptyOrStringBodyResponse.class, null);
    }
    // End Declarations: Request with FormData or URL Encoded FormData //

    // Start Declarations: Request with no request parameter //
    /**
     * 파라미터 없이 비동기 요청을 수행합니다.
     *
     * @param responseType 응답 객체의 클래스 객체
     * @param handler 라이프사이클 핸들러
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <RESP extends IBaseResponse> Mono<RESP> executeAsync(final Class<RESP> responseType, final HttpCallbackHandler<RESP> handler) {
        final ClientContextImpl<IHttpRequest<AUTH>> context = new ClientContextImpl<>(this);
        final Mono<RESP> respMono = authorizedBuilder(getAuthorization())
                .httpRequest(context::setHttpRequest)
                .exchangeToMono(clientResponse -> ClientResponseProcessor.handleResponse(clientResponse, responseType, getRetryCount(), getRetryDelay()));

        return ClientResponseProcessor.applyProcessErrorResumeAndSetCallbackHandler(responseType, handler, context).apply(respMono);
    }

    /**
     * 파라미터 없이 비동기 요청을 수행합니다. (재시도 횟수: 0)
     *
     * @param responseType 응답 객체의 클래스 객체
     * @param <RESP> 응답 객체의 타입
     * @return 응답 객체의 Mono
     */
    public <RESP extends IBaseResponse> Mono<RESP> executeAsync(final Class<RESP> responseType) {
        return executeAsync(responseType, null);
    }

    /**
     * EmptyResponse 로 응답을 받는 비동기 요청을 수행합니다. (재시도 횟수: 0)
     * @param handler 라이프사이클 핸들러
     * @return 응답 객체의 Mono
     */
    public Mono<EmptyOrStringBodyResponse> executeAsync(final HttpCallbackHandler<EmptyOrStringBodyResponse> handler) {
        return executeAsync(EmptyOrStringBodyResponse.class, handler);
    }

    /**
     * EmptyResponse 로 응답을 받는 비동기 요청을 수행합니다. (재시도 횟수: 0)
     * @return 응답 객체의 Mono
     */
    public Mono<EmptyOrStringBodyResponse> executeAsync() {
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
    private WebClient.RequestBodySpec authorizedBuilder(final AUTH auth) {
        final WebClient.RequestBodySpec builder = getWebClient().method(getMethod())
                .uri(uriBuilder -> uriBuilder.path(getUrl())
                        .queryParams(Opt.of(getQueryParam()).orElse(MultiValueMaps.convertMultiValueMap(new HashMap<>())))
                        .build(Opt.of(getPathVariables()).orElse(Collections.emptyMap())))
                .headers(headers -> headers.putAll(Opt.of(getRequestHeaders()).orElse(MultiValueMaps.convertMultiValueMap(new HashMap<>()))));
        authorize(builder, auth);
        return builder;
    }

}
