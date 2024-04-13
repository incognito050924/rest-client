package io.incognito.rest.client.exceptions;

import io.incognito.rest.client.types.dto.ApiResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiFailureException extends RuntimeException {
    private ApiResult failureResult;

    public ApiFailureException(final ApiResult failureResult) {
        setFailureResult(failureResult);
    }

    public ApiFailureException(final ApiResult failureResult, final String message) {
        super(message);
        setFailureResult(failureResult);
    }

    public ApiFailureException(final ApiResult failureResult, final String message, final Throwable cause) {
        super(message, cause);
        setFailureResult(failureResult);
    }

    public ApiFailureException(final ApiResult failureResult, final Throwable cause) {
        super(cause);
        setFailureResult(failureResult);
    }
}
