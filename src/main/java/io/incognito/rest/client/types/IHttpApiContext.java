package io.incognito.rest.client.types;

import org.springframework.http.client.reactive.ClientHttpRequest;

import io.incognito.rest.client.IHttpRequest;

public interface IHttpApiContext<R extends IHttpRequest<?>> {
    R getRequestConfig();
    void setRequestConfig(R requestConfig);

    ClientHttpRequest getHttpRequest();
    void setHttpRequest(ClientHttpRequest httpRequest);
}
