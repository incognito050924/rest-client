package io.incognito.rest.client.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.SerializationUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Maps {

    /**
     * Returns a new map that matches the keys and values of the source map.
     *
     * @param source the source map
     * @param matcher map to match (partial match)
     * @return the new map
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static <K, V> Map<K, V> find(final Map<K, V> source, final Map<? super K, ? super V> matcher) {
        final BiPredicate<? super K, ? super V> predicate;

        if (matcher == null || matcher.isEmpty()) {
            predicate = (k, v) -> false;
        } else {
            predicate = (k, v) -> matcher.entrySet()
                    .stream()
                    .anyMatch(entry ->
                            // check equality of key
                            Opt.of(entry.getKey()).map(entryKey -> entryKey.equals(k)).orElse(k == null)
                            &&
                            // check equality of value
                            Opt.of(entry.getValue()).map(entryValue -> entryValue.equals(v)).orElse(v == null)
                    );
        }

        return find(source, predicate, null);
    }

    /**
     * Returns a new map that contains only the keys that satisfy the predicate.
     *
     * @param source the source map
     * @param matcher the predicate
     * @return the new map
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static <K, V> Map<K, V> find(final Map<K, V> source, final BiPredicate<? super K, ? super V> matcher) {
        return find(source, matcher, null);
    }

    /**
     * Returns a new map that contains only the keys that satisfy the predicate.
     *
     * @param source the source map
     * @param matcher the predicate
     * @param constructor the constructor to create a new map
     * @return the new map
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static <K, V> Map<K, V> find(final Map<K, V> source, final BiPredicate<? super K, ? super V> matcher, final Supplier<Map<K, V>> constructor) {
        final Map<K, V> result = Opt.of(constructor).map(Supplier::get).orElseGet(ConcurrentHashMap::new);
        Opt.of(source)
                .map(Map::entrySet)
                .stream()
                .flatMap(java.util.Collection::stream)
                .filter(entry -> matcher.test(entry.getKey(), entry.getValue()))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return deepCopy(result);
    }

    /**
     * Returns a stream of values that satisfy the predicate.
     *
     * @param source the source map
     * @param matcher the predicate
     * @return the stream of values
     * @param <V> the type of values
     */
    public static <V> Stream<V> findValues(final Map<String, V> source, final BiPredicate<? super String, ? super V> matcher) {
        return Opt.of(source)
                .map(Maps::deepCopy)
                .map(Map::entrySet)
                .stream()
                .flatMap(java.util.Collection::stream)
                .filter(entry -> matcher.test(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getValue);
    }

    /**
     * Returns the value of the key in the map.
     *
     * @param source the source map
     * @param key the key which value to get
     * @param type the type of the value
     * @return the value
     * @param <V> the type of the value
     */
    public static <V> Optional<V> getValue(final Map<String, ?> source, final String key, final Class<V> type) {
        return findValues(source, (k, v) -> key.equals(k))
                .findFirst()
                .flatMap(value -> {
                    try {
                        return Optional.of(type != null ? type.cast(value) : (V) value);
                    } catch (final Exception ignore) {}
                    return Optional.empty();
                });
    }

    /**
     * Returns the value of the key in the map.
     *
     * @param source the source map
     * @param key the key which value to get
     * @param type the type of the value
     * @return the value
     * @param <V> the type of the value
     */
    public static <V> Optional<V> getValue(final Map<String, ?> source, final String key, final TypeReference<V> type) {
        return getValue(source, key, type, null);
    }

    /**
     * Returns the value of the key in the map.
     *
     * @param source the source map
     * @param key the key which value to get
     * @param type the type of the value
     * @param objectMapper the object mapper
     * @return the value
     * @param <V> the type of the value
     */
    public static <V> Optional<V> getValue(final Map<String, ?> source, final String key, final TypeReference<V> type, final ObjectMapper objectMapper) {
        return findValues(source, (k, v) -> key.equals(k))
                .findFirst()
                .flatMap(value -> {
                    try {
                        return Optional.ofNullable(type != null ? Opt.of(objectMapper).orElseGet(ObjectMapper::new).convertValue(value, type) : (V) value);
                    } catch (final Exception ignore) {}
                    return Optional.empty();
                });
    }

    /**
     * Returns a new map that contains only the keys specified in the includeKeys.
     *
     * @param source the source map
     * @param includeKeys the keys to include
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> pickByKeys(final Map<String, V> source, final String... includeKeys) {
        return pickByKeys(source, Arrays.asList(includeKeys));
    }

    /**
     * Returns a new map that contains only the keys specified in the includeKeys.
     *
     * @param source the source map
     * @param includeKeys the keys to include
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> pickByKeys(final Map<String, V> source, final List<String> includeKeys) {
        return pickByKeys(source, includeKeys, null);
    }

    /**
     * Returns a new map that contains only the keys specified in the includeKeys.
     *
     * @param source the source map
     * @param includeKeys the keys to include
     * @param constructor the constructor to create a new map
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> pickByKeys(final Map<String, V> source, final List<String> includeKeys, final Supplier<Map<String, V>> constructor) {
        if (source == null) {
            return null;
        }
        return find(source, (k, v) -> Opt.of(includeKeys).orElse(Collections.emptyList()).contains(k), constructor);
    }

    /**
     * Returns a new map that contains only the keys not specified in the excludeKeys.
     *
     * @param source the source map
     * @param excludeKeys the keys to exclude
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> omitByKeys(final Map<String, V> source, final String... excludeKeys) {
        return omitByKeys(source, Arrays.asList(excludeKeys));
    }

    /**
     * Returns a new map that contains only the keys not specified in the excludeKeys.
     *
     * @param source the source map
     * @param excludeKeys the keys to exclude
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> omitByKeys(final Map<String, V> source, final List<String> excludeKeys) {
        return omitByKeys(source, excludeKeys, null);
    }

    /**
     * Returns a new map that contains only the keys not specified in the excludeKeys.
     *
     * @param source the source map
     * @param excludeKeys the keys to exclude
     * @param constructor the constructor to create a new map
     * @return the new map
     * @param <V> the type of values
     */
    public static <V> Map<String, V> omitByKeys(final Map<String, V> source, final List<String> excludeKeys, final Supplier<Map<String, V>> constructor) {
        if (source == null) {
            return null;
        }

        return find(source, (k, v) -> !Opt.of(excludeKeys).orElse(Collections.emptyList()).contains(k), constructor);
    }

    /**
     * Converts the object to a map.
     *
     * @param source the source object
     * @return the map
     * @param <T> the type of the source object
     */
    public static <T> Map<String, Object> convert2map(final T source) {
        return convert2map(source, new ObjectMapper());
    }

    /**
     * Converts the object to a map.
     *
     * @param source the source object
     * @param objectMapper the object mapper
     * @return the map
     * @param <T> the type of the source object
     */
    public static <T> Map<String, Object> convert2map(final T source, final ObjectMapper objectMapper) {
        return objectMapper.convertValue(source, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Converts the map to an object.
     *
     * @param source the source map
     * @param clazz the class of the object
     * @return the object
     * @param <T> the type of the object
     */
    public static <T> T convert2object(final Map<String, Object> source, final Class<T> clazz) {
        return convert2object(source, clazz, new ObjectMapper());
    }

    /**
     * Converts the map to an object.
     *
     * @param source the source map
     * @param clazz the class of the object
     * @param objectMapper the object mapper
     * @return the object
     * @param <T> the type of the object
     */
    public static <T> T convert2object(final Map<String, Object> source, final Class<T> clazz, final ObjectMapper objectMapper) {
        return objectMapper.convertValue(source, clazz);
    }

    /**
     * Makes the map immutable.
     *
     * @param source the source map
     * @return the immutable map
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static <K, V> Map<K, V> freeze(final Map<K, V> source) {
        return Collections.unmodifiableMap(source);
    }

    /**
     * Deep copy the map.
     *
     * @param source the source map
     * @return the copied map
     * @param <K> the type of keys
     * @param <V> the type of values
     */
    public static <K, V> Map<K, V> deepCopy(final Map<K, V> source) {
        return SerializationUtils.clone(new HashMap<>(source));
    }
}
