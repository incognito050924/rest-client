package io.incognito.rest.client.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * Opt class is a utility class that provides a way to handle null values in a more functional way.
 * complementing the Optional class in Java.
 *
 * @param <T>
 */
@RequiredArgsConstructor
public class Opt<T> implements Serializable, Comparable<Opt<T>> {
    private final T _value;

    /**
     * Create an Opt instance with the given value.
     *
     * @param value the value to be wrapped
     * @return Opt instance or empty Opt instance when the value is null
     * @param <T> the type of the value
     */
    public static <T> Opt<T> of(final T value) {
        return new Opt<>(value);
    }

    /**
     * Create an empty Opt instance.
     *
     * @return empty Opt instance
     * @param <T> the type of the value
     */
    public static <T> Opt<T> empty() {
        return new Opt<>(null);
    }

    /**
     * Create an Opt instance with the given value.
     *
     * @param value the value to be wrapped
     * @return Opt instance or throw NullPointerException when the value is null
     * @param <T> the type of the value
     * @throws NullPointerException when the value is null
     */
    public static <T> Opt<T> ofNonNull(final T value) {
        return new Opt<>(Objects.requireNonNull(value, "value cannot be null."));
    }

    /**
     * Create an Opt instance with the value returned by the given supplier.
     *
     * @param supplier the supplier to provide the value
     * @return Opt instance or empty Opt instance when the value is null
     * @param <T> the type of the value
     */
    public static <T> Opt<T> from(final Supplier<T> supplier) {
        return new Opt<>(supplier.get());
    }

    /**
     * Check if the value is present.
     *
     * @return true if the value is present, false otherwise
     */
    public boolean isPresent() {
        return _value != null;
    }

    /**
     * Check if the value is not present.
     *
     * @return true if the value is not present, false otherwise
     */
    public boolean isEmpty() {
        return !isPresent();
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public T get() {
        return _value;
    }

    /**
     * Get the value or the given value if the value is null.
     *
     * @param other the value to be returned if the value is null
     * @return the value or the given value if the value is null
     */
    public T orElse(final T other) {
        return isPresent() ? _value : other;
    }

    /**
     * Get the value or the value returned by the given supplier if the value is null.
     *
     * @param other the supplier to provide the value if the value is null
     * @return the value or the value returned by the given supplier if the value is null
     */
    public T orElseGet(final Supplier<T> other) {
        return isPresent() ? _value : other.get();
    }

    /**
     * Get the value or throw the exception returned by the given supplier if the value is null.
     *
     * @param exceptionSupplier the supplier to provide the exception if the value is null
     * @return the value or throw the exception returned by the given supplier if the value is null
     * @param <X> the type of the exception
     * @throws X the exception returned by the given supplier
     */
    public <X extends Throwable> T orElseThrow(final Supplier<X> exceptionSupplier) throws X {
        if (isPresent()) {
            return _value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Map the value to another value using the given mapper.
     *
     * @param mapper the mapper to map the value
     * @return Opt instance or empty Opt instance when the value is null
     * @param <U> the type of the mapped value
     */
    public <U> Opt<U> map(final Function<? super T, ? extends U> mapper) {
        return isPresent() ? Opt.of(mapper.apply(_value)) : Opt.empty();
    }

    /**
     * Map the value to another value using the given mapper.
     *
     * @param mapper the mapper to map the value
     * @return Opt instance or empty Opt instance when the value is null
     * @param <U> the type of the mapped value
     */
    public <U> Opt<U> flatMap(final Function<? super T, Opt<U>> mapper) {
        return isPresent() ? mapper.apply(_value) : Opt.empty();
    }

    /**
     * Filter the value using the given predicate.
     *
     * @param predicate the predicate to filter the value
     * @return Opt instance or empty Opt instance when the value is null
     */
    public Opt<T> filter(final Function<? super T, Boolean> predicate) {
        return isPresent() && predicate.apply(_value) ? this : Opt.empty();
    }

    /**
     * Filter the value using the given predicate.
     *
     * @param predicate the predicate to filter the value
     * @return Opt instance or empty Opt instance when the value is null
     */
    public Opt<T> filterNot(final Function<? super T, Boolean> predicate) {
        return isPresent() && !predicate.apply(_value) ? this : Opt.empty();
    }

    /**
     * If the value is present, perform the given action.
     *
     * @param consumer the action to be performed
     */
    public void ifPresent(final Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(_value);
        }
    }

    /**
     * Makes a Stream of the value.
     *
     * @return Stream of the value or empty Stream when the value is not present
     */
    public Stream<T> stream() {
        return isPresent() ? Stream.of(_value) : Stream.empty();
    }

    /**
     * Convert the value to Optional.
     *
     * @return Optional instance or empty Optional instance when the value is null
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(get());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Opt<?> other = (Opt<?>) obj;
        return Objects.equals(get(), other.get());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public String toString() {
        if (_value == null) {
            return "Empty Opt";
        }
        return "Opt {" + _value + "}";
    }

    @Override
    public int compareTo(final Opt<T> o) {
        if (o.isEmpty()) {
            return isPresent() ? 1 : 0;
        } else if (isEmpty()) {
            return -1;
        }

        final T o2 = o.get();
        if (_value instanceof Comparable) {
            return ((Comparable<T>) _value).compareTo(o2);
        }

        if (o2 instanceof Comparable) {
            return -1 * ((Comparable<T>) o2).compareTo(_value);
        }

        if (_value != null) {
            return _value.equals(o2) ? 0 : 1;
        }

        return 1;
    }
}
