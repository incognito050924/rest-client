package io.incognito.rest.client.types.dto;

import org.springframework.http.client.reactive.ClientHttpRequest;

import io.incognito.rest.client.HttpClientExecutors;
import io.incognito.rest.client.IHttpRequest;
import io.incognito.rest.client.types.IHttpApiContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientContextImpl<R extends IHttpRequest<?>> implements IHttpApiContext<R> {
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
