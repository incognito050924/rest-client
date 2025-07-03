package io.incognito.rest.client;

import org.springframework.web.reactive.function.client.WebClient;

@FunctionalInterface
public interface AuthorizationApplier<AUTH> {
    /**
     * Applies the authorization to the HTTP request.
     *
     * @param httpRequest the HTTP request to which the authorization should be applied
     * @param auth the authorization object
     */
    <S extends WebClient.RequestHeadersSpec<?>> void apply(S httpRequest, AUTH auth);
}
