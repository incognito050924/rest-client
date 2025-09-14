package io.incognito.rest.client;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

import io.incognito.rest.client.config.DefaultKotlinCompatibleHttpClientConfigurer;
import lombok.Builder;
import lombok.Getter;

/**
 * Kotlin 사용자를 위한 편의 클래스
 * Kotlin 호환성이 기본으로 적용된 WebClient와 HttpClients를 제공합니다.
 */
@Getter
public class KotlinHttpClients<AUTH> extends HttpClientExecutors<AUTH> {

    @Builder
    protected KotlinHttpClients(WebClient webClient, HttpMethod method, String url,
                               MultiValueMap<String, String> requestHeaders,
                               MultiValueMap<String, String> queryParam,
                               Map<String, String> pathVariables,
                               AUTH authorization,
                               AuthorizationApplier<AUTH> authorizationApplier,
                               int retryCount,
                               Duration retryDelay) {
        super(webClient, method, url, requestHeaders, queryParam, pathVariables, authorization, authorizationApplier, retryCount, retryDelay);
    }

    /**
     * Kotlin 호환성이 적용된 기본 WebClient를 생성합니다.
     *
     * @return Kotlin 호환성이 적용된 WebClient
     */
    public static WebClient createKotlinCompatibleWebClient() {
        DefaultKotlinCompatibleHttpClientConfigurer configurer = new DefaultKotlinCompatibleHttpClientConfigurer();
        return configurer.apiWebClient(null, null);
    }

    /**
     * Kotlin 호환성이 적용된 기본 WebClient로 GET 요청 빌더를 생성합니다.
     *
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> get() {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(createKotlinCompatibleWebClient())
                .method(HttpMethod.GET);
    }

    /**
     * 지정된 WebClient로 GET 요청 빌더를 생성합니다.
     *
     * @param webClient 사용할 WebClient
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> get(final WebClient webClient) {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.GET);
    }

    /**
     * Kotlin 호환성이 적용된 기본 WebClient로 POST 요청 빌더를 생성합니다.
     *
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> post() {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(createKotlinCompatibleWebClient())
                .method(HttpMethod.POST);
    }

    /**
     * 지정된 WebClient로 POST 요청 빌더를 생성합니다.
     *
     * @param webClient 사용할 WebClient
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> post(final WebClient webClient) {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.POST);
    }

    /**
     * Kotlin 호환성이 적용된 기본 WebClient로 PUT 요청 빌더를 생성합니다.
     *
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> put() {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(createKotlinCompatibleWebClient())
                .method(HttpMethod.PUT);
    }

    /**
     * 지정된 WebClient로 PUT 요청 빌더를 생성합니다.
     *
     * @param webClient 사용할 WebClient
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> put(final WebClient webClient) {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.PUT);
    }

    /**
     * Kotlin 호환성이 적용된 기본 WebClient로 DELETE 요청 빌더를 생성합니다.
     *
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> delete() {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(createKotlinCompatibleWebClient())
                .method(HttpMethod.DELETE);
    }

    /**
     * 지정된 WebClient로 DELETE 요청 빌더를 생성합니다.
     *
     * @param webClient 사용할 WebClient
     * @param <AUTH> 인증 타입
     * @return HttpClients 빌더
     */
    public static <AUTH> KotlinHttpClientsBuilder<AUTH> delete(final WebClient webClient) {
        return KotlinHttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.DELETE);
    }
}