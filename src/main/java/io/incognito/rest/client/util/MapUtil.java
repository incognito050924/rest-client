package io.incognito.rest.client.util;

import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtil {

    public static <K, V> Map<K, List<V>> toMap(final MultiValueMap<K, V> multiValueMap) {
        return multiValueMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K, V> Map<K, V> toSingleValueMap(final MultiValueMap<K, V> multiValueMap) {
        return multiValueMap.toSingleValueMap();
    }

    public static <K, V> MultiValueMap<K, V> toMultiValueMap(final Map<K, List<V>> map) {
        return new MultiValueMapAdapter<>(map);
    }
    
    public static <K, V> MultiValueMap<K, V> convertMultiValueMap(final Map<K, V> map) {
        return toMultiValueMap(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, element -> {
            final List<V> list = new ArrayList<>();
            list.add(element.getValue());
            return list;
        })));
    }
}
