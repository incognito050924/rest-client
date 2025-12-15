package io.incognito.rest.client.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Jackson deserializer for {@link MultiValueMap}.
 * <p>
 * This deserializer converts JSON map structures (where values are arrays of strings)
 * into Spring's {@link LinkedMultiValueMap} implementation.
 * </p>
 *
 * <p>Example JSON input:</p>
 * <pre>
 * {
 *   "Content-Type": ["application/json"],
 *   "X-Custom-Header": ["value1", "value2"]
 * }
 * </pre>
 */
public class MultiValueMapDeserializer extends JsonDeserializer<MultiValueMap<String, String>> {

    private static final TypeReference<Map<String, List<String>>> TYPE_REFERENCE =
            new TypeReference<>() {};

    @Override
    public MultiValueMap<String, String> deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException {
        final Map<String, List<String>> map = p.readValueAs(TYPE_REFERENCE);
        if (map == null) {
            return null;
        }
        return new LinkedMultiValueMap<>(map);
    }
}
