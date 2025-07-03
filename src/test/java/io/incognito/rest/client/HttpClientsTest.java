package io.incognito.rest.client;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;

public class HttpClientsTest{

    @Test
    public void testEmptyBodyAndEmptyResponse() {

        final EmptyOrStringBodyResponse resp = HttpClients.builder()
                .build()
                .executeAsync()
                .block();
    }

}