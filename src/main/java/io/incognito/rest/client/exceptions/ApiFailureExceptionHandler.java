package io.incognito.rest.client.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Consumer;
import java.util.function.Function;

import io.incognito.rest.client.types.dto.ApiResult;
import io.incognito.rest.client.util.Opt;

public interface ApiFailureExceptionHandler {

    /**
     * Handle the {@link ApiFailureException} and convert it to {@link ResponseEntity} with the given {@code convertResult} function.
     * @param e the exception
     * @param convertResult the function to convert the {@link ApiResult} to the response body
     * @param isDebug whether to show the failure detail
     * @param <T> the response body type
     * @return the response entity
     */
    static <T> ResponseEntity<T> handleApiFailureException(final ApiFailureException e, final Function<ApiResult, T> convertResult, final boolean isDebug) {
        final ApiResult result = e.getFailureResult();
        final T body = convertResult.apply(result);
        if (isDebug) {
            result.setFailureDetail(null);
        }
        return ResponseEntity.status(Opt.of(result.getStatus()).orElse(HttpStatus.INTERNAL_SERVER_ERROR)).body(body);
    }

    /**
     * Handle the {@link ApiFailureException} and convert it to {@link ResponseEntity} with the given {@code convertResult} function.
     * @param e the exception
     * @param consumer the consumer to modify or peek the {@link ApiResult}
     * @param isDebug whether to show the failure detail
     * @return the response entity
     */
    static ResponseEntity<ApiResult> handleApiFailureException(final ApiFailureException e, final Consumer<ApiResult> consumer, final boolean isDebug) {
        final Function<ApiResult, ApiResult> consumeAndReturn = result -> {
            Opt.of(consumer).ifPresent(c -> c.accept(result));
            return result;
        };
        return handleApiFailureException(e, consumeAndReturn, isDebug);
    }

    /**
     * Handle the {@link ApiFailureException} and convert it to {@link ResponseEntity} with the given {@code convertResult} function.
     * @param e the exception
     * @param isDebug whether to show the failure detail
     * @return the response entity
     */
    static ResponseEntity<ApiResult> handleApiFailureException(final ApiFailureException e, final boolean isDebug) {
        return handleApiFailureException(e, Function.identity(), isDebug);
    }
}
