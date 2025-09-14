package io.incognito.rest.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.netty.ConnectionObserver;

/**
 * Kotlin 호환성을 기본으로 제공하는 HttpClientConfigurer 구현체
 * 사용자가 별도의 ObjectMapper 설정 없이도 Kotlin과 호환되는 HTTP 클라이언트를 사용할 수 있도록 합니다.
 */
public class DefaultKotlinCompatibleHttpClientConfigurer extends HttpClientConfigurer {

    public DefaultKotlinCompatibleHttpClientConfigurer() {
        this(30, 30, 30, 1024 * 1024, 100);
    }

    public DefaultKotlinCompatibleHttpClientConfigurer(
            int connectionTimeoutSeconds,
            int readTimeoutSeconds,
            int writeTimeoutSeconds,
            int maxContentLength,
            int maxConnections) {
        super(connectionTimeoutSeconds, readTimeoutSeconds, writeTimeoutSeconds, maxContentLength, maxConnections);
    }

    @Override
    public ConnectionObserver connectionObserver() {
        return ConnectionObserver.emptyListener();
    }

    @Override
    public ObjectMapper webClientObjectMapper() {
        // Kotlin 호환성이 적용된 기본 ObjectMapper 사용
        return createKotlinCompatibleMapper();
    }
}