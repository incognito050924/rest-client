package io.incognito.rest.client.handler;

import io.incognito.rest.client.HttpClientExecutors;
import io.incognito.rest.client.types.IHttpApiContext;
import io.incognito.rest.client.types.dto.response.IBaseResponse;
import io.incognito.rest.client.util.Opt;
import reactor.core.publisher.SignalType;

public interface HttpCallbackHandler<RESP extends IBaseResponse> {
    /**
     * 응답 수신 시 수행할 로직
     * @param response 응답
     */
    default <C extends IHttpApiContext<?>> void onResponse(final RESP response, final C context) {
        Opt.of(response).ifPresent(resp -> {
            if (resp.isSuccess()) {
                onSuccess(resp, context);
            } else {
                onFailed(resp, context);
            }
        });
    }

    /**
     * 성공 응답 수신 시 수행할 로직
     *
     * @param response 성공 응답
     */
    <C extends IHttpApiContext<?>> void onSuccess(RESP response, C context);

    /**
     * 실패 응답 수신 시 수행할 로직
     *
     * @param response 실패 응답
     */
    <C extends IHttpApiContext<?>> void onFailed(RESP response, C context);

    /**
     * HTTP 요청/응답 처리 중 예외 발생 시 수행할 로직
     * @param t 예외
     */
    <C extends IHttpApiContext<?>> void onError(Throwable t, C context);

    /**
     * HTTP 요청 / 응답 종료 이후 수행할 로직
     *
     * @param signalType 처리 결과 타입
     */
    <C extends IHttpApiContext<?>> void afterFinished(SignalType signalType, C Context);
}
