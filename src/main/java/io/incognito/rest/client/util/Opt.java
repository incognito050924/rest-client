package io.incognito.rest.client.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Opt<T> {
    private final T _value;

    public static <T> Opt<T> of(final T value) {
        return new Opt<>(value);
    }

    public static <T> Opt<T> empty() {
        return new Opt<>(null);
    }

    public static <T> Opt<T> ofNonNull(final T value) {
        return new Opt<>(Objects.requireNonNull(value, "value cannot be null."));
    }

    public static <T> Opt<T> from(final Supplier<T> supplier) {
        return new Opt<>(supplier.get());
    }

    public boolean isPresent() {
        return _value != null;
    }

    public T get() {
        return _value;
    }

    public T orElse(final T other) {
        return isPresent() ? _value : other;
    }

    public T orElseGet(final Supplier<T> other) {
        return isPresent() ? _value : other.get();
    }

    public <X extends Throwable> T orElseThrow(final Supplier<X> exceptionSupplier) throws X {
        if (isPresent()) {
            return _value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public <U> Opt<U> map(final Function<? super T, ? extends U> mapper) {
        return isPresent() ? Opt.of(mapper.apply(_value)) : Opt.empty();
    }

    public <U> Opt<U> flatMap(final Function<? super T, Opt<U>> mapper) {
        return isPresent() ? mapper.apply(_value) : Opt.empty();
    }

    public Opt<T> filter(final Function<? super T, Boolean> predicate) {
        return isPresent() && predicate.apply(_value) ? this : Opt.empty();
    }

    public Opt<T> filterNot(final Function<? super T, Boolean> predicate) {
        return isPresent() && !predicate.apply(_value) ? this : Opt.empty();
    }

    public void ifPresent(final Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(_value);
        }
    }

    public Stream<T> stream() {
        return isPresent() ? Stream.of(_value) : Stream.empty();
    }

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
}
