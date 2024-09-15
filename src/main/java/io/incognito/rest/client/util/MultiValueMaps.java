package io.incognito.rest.client.util;

import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultiValueMaps {

    /**
     * Convert MultiValueMap to Map with single value.
     *
     * @param multiValueMap MultiValueMap
     * @return Map
     * @param <K> Type of keys
     * @param <V> Type of values
     */
    public static <K, V> Map<K, V> toSingleValueMap(final MultiValueMap<K, V> multiValueMap) {
        return multiValueMap.toSingleValueMap();
    }

    /**
     * Convert Map to MultiValueMap.
     *
     * @param map Map
     * @return MultiValueMap
     * @param <K> Type of keys
     * @param <V> Type of values
     */
    public static <K, V> MultiValueMap<K, V> toMultiValueMap(final Map<K, List<V>> map) {
        return new MultiValueMapAdapter<>(map);
    }

    /**
     * Convert Map to MultiValueMap with single value.
     *
     * @param map Map
     * @return MultiValueMap
     * @param <K> Type of keys
     * @param <V> Type of values
     */
    public static <K, V> MultiValueMap<K, V> convertMultiValueMap(final Map<K, V> map) {
        return toMultiValueMap(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, element -> {
            final List<V> list = new ArrayList<>();
            list.add(element.getValue());
            return list;
        })));
    }
}
