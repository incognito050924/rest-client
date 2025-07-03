package io.incognito.rest.client;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
public class HttpClients<AUTH> extends HttpClientExecutors<AUTH> {

    @Builder
    protected HttpClients(WebClient webClient, HttpMethod method, String url, MultiValueMap<String, String> requestHeaders, MultiValueMap<String, String> queryParam, Map<String, String> pathVariables, AUTH authorization, AuthorizationApplier<AUTH> authorizationApplier, int retryCount, Duration retryDelay) {
        super(webClient, method, url, requestHeaders, queryParam, pathVariables, authorization, authorizationApplier, retryCount, retryDelay);
    }

    public static <AUTH> HttpClientsBuilder<AUTH> get(final WebClient webClient) {
        return HttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.GET);
    }

    public static <AUTH> HttpClientsBuilder<AUTH> post(final WebClient webClient) {
        return HttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.POST);
    }

    public static <AUTH> HttpClientsBuilder<AUTH> put(final WebClient webClient) {
        return HttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.PUT);
    }

    public static <AUTH> HttpClientsBuilder<AUTH> delete(final WebClient webClient) {
        return HttpClients.<AUTH>builder()
                .webClient(webClient)
                .method(HttpMethod.DELETE);
    }
}
