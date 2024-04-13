package io.incognito.rest.client;

public interface IHttpClientBuilder<AUTH> extends IHttpClient<AUTH> {
    boolean validate();
    IHttpClientExecutor<AUTH> build();
}
