package io.incognito.rest.client.types.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import io.incognito.rest.client.types.enums.ApiResultCode;

import static org.junit.jupiter.api.Assertions.*;

class ApiResultTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("ApiResult with MultiValueMap should serialize and deserialize correctly")
    void shouldSerializeAndDeserializeMultiValueMap() throws JsonProcessingException {
        // Given
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("X-Custom-Header", "value1");
        headers.add("X-Custom-Header", "value2");

        final ApiResult original = ApiResult.builder()
                .status(HttpStatus.OK)
                .responseHeaders(headers)
                .resultCode(ApiResultCode.SUCCESS)
                .build();

        // When
        final String json = objectMapper.writeValueAsString(original);
        final ApiResult deserialized = objectMapper.readValue(json, ApiResult.class);

        // Then
        assertNotNull(deserialized);
        assertNotNull(deserialized.getResponseHeaders());
        assertEquals(List.of("application/json"), deserialized.getResponseHeaders().get("Content-Type"));
        assertEquals(List.of("value1", "value2"), deserialized.getResponseHeaders().get("X-Custom-Header"));
    }

    @Test
    @DisplayName("ApiResult with null MultiValueMap should serialize and deserialize correctly")
    void shouldHandleNullMultiValueMap() throws JsonProcessingException {
        // Given
        final ApiResult original = ApiResult.builder()
                .status(HttpStatus.OK)
                .responseHeaders(null)
                .resultCode(ApiResultCode.SUCCESS)
                .build();

        // When
        final String json = objectMapper.writeValueAsString(original);
        final ApiResult deserialized = objectMapper.readValue(json, ApiResult.class);

        // Then
        assertNotNull(deserialized);
        assertNull(deserialized.getResponseHeaders());
    }

    @Test
    @DisplayName("ApiResult with empty MultiValueMap should serialize and deserialize correctly")
    void shouldHandleEmptyMultiValueMap() throws JsonProcessingException {
        // Given
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        final ApiResult original = ApiResult.builder()
                .status(HttpStatus.OK)
                .responseHeaders(headers)
                .resultCode(ApiResultCode.SUCCESS)
                .build();

        // When
        final String json = objectMapper.writeValueAsString(original);
        final ApiResult deserialized = objectMapper.readValue(json, ApiResult.class);

        // Then
        assertNotNull(deserialized);
        assertNotNull(deserialized.getResponseHeaders());
        assertTrue(deserialized.getResponseHeaders().isEmpty());
    }
}
