package io.incognito.rest.client.types.dto.response;

import io.incognito.rest.client.types.dto.ApiResult;
import io.incognito.rest.client.types.enums.ApiResultCode;
import io.incognito.rest.client.util.Opt;

public interface IBaseResponse {
    ApiResult getApiResult();

    void setApiResult(ApiResult apiResult);

    default boolean isSuccess() {
        return Opt.of(getApiResult())
                .flatMap(apiResult -> Opt.of(apiResult.getResultCode()).filter(ApiResultCode.SUCCESS::equals))
                .isPresent();
    }
}
