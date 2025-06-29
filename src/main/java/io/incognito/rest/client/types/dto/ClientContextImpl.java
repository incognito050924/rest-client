package io.incognito.rest.client.types.dto;

import org.springframework.http.client.reactive.ClientHttpRequest;

import io.incognito.rest.client.IHttpClientExecutor;
import io.incognito.rest.client.IHttpRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientContextImpl<R extends IHttpRequest<?>> implements IHttpClientExecutor.Context<R> {
    private R requestConfig;
    private ClientHttpRequest httpRequest;

    public ClientContextImpl(final R requestConfig, final ClientHttpRequest httpRequest) {
        this.requestConfig = requestConfig;
        this.httpRequest = httpRequest;
    }

    public ClientContextImpl(final R requestConfig) {
        this(requestConfig, null);
    }
}
