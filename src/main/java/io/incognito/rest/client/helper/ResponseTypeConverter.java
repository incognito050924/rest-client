package io.incognito.rest.client.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.incognito.rest.client.exceptions.ApiFailureException;
import io.incognito.rest.client.types.dto.ApiResult;
import io.incognito.rest.client.types.dto.response.BaseApiResponse;
import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.enums.ApiResultCode;
import io.incognito.rest.client.util.Opt;
import lombok.RequiredArgsConstructor;

public class ResponseTypeConverter {

    public static <R1 extends BaseApiResponse, R2 extends BaseApiResponse> R2 convertResponseType(final R1 response, final Class<R2> targetType, final ObjectMapper objectMapper) {
        return new ResponseTypeMapper(objectMapper).convertResponseType(response, targetType);
    }

    public static <R1 extends BaseApiResponse, R2 extends BaseApiResponse> R2 convertResponseType(final R1 response, final TypeReference<R2> targetType, final ObjectMapper objectMapper) {
        return new ResponseTypeMapper(objectMapper).convertResponseType(response, targetType);
    }

    public static <R extends BaseApiResponse> R convertEmptyOrStringBodyResponseType(final EmptyOrStringBodyResponse response, final Class<R> targetType, final ObjectMapper objectMapper) {
        return new ResponseTypeMapper(objectMapper).convertResponseType(response, targetType);
    }

    public static <R extends BaseApiResponse> R convertEmptyOrStringBodyResponseType(final EmptyOrStringBodyResponse response, final TypeReference<R> targetType, final ObjectMapper objectMapper) {
        return new ResponseTypeMapper(objectMapper).convertResponseType(response, targetType);
    }

    @RequiredArgsConstructor
    static class ResponseTypeMapper {
        private final ObjectMapper objectMapper;

        <R1 extends BaseApiResponse, R2 extends BaseApiResponse> R2 convertResponseType(final R1 response, final Class<R2> targetType) {
            try {
                if (response == null) {
                    return null;
                }

                final ApiResult apiResult = response.getApiResult();
                response.setApiResult(null); // Clear apiResult to avoid circular reference
                final R2 converted = objectMapper.convertValue(response, targetType);
                converted.setApiResult(apiResult); // Restore apiResult after conversion
                return converted;
            } catch (Exception e) {
                throw createApiFailureException(response, targetType, e);
            }
        }

        <R1 extends BaseApiResponse, R2 extends BaseApiResponse> R2 convertResponseType(final R1 response, final TypeReference<R2> targetType) {
            try {
                if (response == null) {
                    return null;
                }

                final ApiResult apiResult = response.getApiResult();
                response.setApiResult(null); // Clear apiResult to avoid circular reference
                final R2 converted = objectMapper.convertValue(response, targetType);
                converted.setApiResult(apiResult); // Restore apiResult after conversion
                return converted;
            } catch (Exception e) {
                throw createApiFailureException(response, targetType, e);
            }
        }

        private <R1 extends BaseApiResponse, R2 extends BaseApiResponse> ApiFailureException createApiFailureException(final R1 response, final TypeReference<R2> targetType, final Exception e) {
            final String errorMessage = String.format("Failed to convert response type. Reason: %s", e.getMessage());
            final String errorDetail = String.format("Response type Conversion (%s -> %s)\nValue: %s", response.getClass().getCanonicalName(), Opt.of(targetType.getType()).map(Type::getTypeName).orElse(null), response);
            final String errorStackTrace = Opt.of(e.getStackTrace()).stream().flatMap(Arrays::stream).map(StackTraceElement::toString).collect(Collectors.joining("\n", "------------------------------------------- Stack Trace -------------------------------------------\n", "\n---------------------------------------------------------------------------------------------------"));
            return new ApiFailureException(Opt.of(response.getApiResult()).orElseGet(() -> ApiResult.builder().resultCode(ApiResultCode.FAILED_TO_DESERIALIZE).failureMessage(errorMessage).failureDetail(String.format("%s\n%s", errorDetail, errorStackTrace)).build()), e);
        }

        private <R1 extends BaseApiResponse, R2 extends BaseApiResponse> ApiFailureException createApiFailureException(final R1 response, final Class<R2> targetType, final Exception e) {
            final String errorMessage = String.format("Failed to convert response type. Reason: %s", e.getMessage());
            final String errorDetail = String.format("Response type Conversion (%s -> %s)\nValue: %s", response.getClass().getCanonicalName(), Opt.of(targetType).map(Class::getCanonicalName).orElse(null), response);
            final String errorStackTrace = Opt.of(e.getStackTrace()).stream().flatMap(Arrays::stream).map(StackTraceElement::toString).collect(Collectors.joining("\n", "------------------------------------------- Stack Trace -------------------------------------------\n", "\n---------------------------------------------------------------------------------------------------"));
            return new ApiFailureException(Opt.of(response.getApiResult()).orElseGet(() -> ApiResult.builder().resultCode(ApiResultCode.FAILED_TO_DESERIALIZE).failureMessage(errorMessage).failureDetail(String.format("%s\n%s", errorDetail, errorStackTrace)).build()), e);
        }
    }
}
