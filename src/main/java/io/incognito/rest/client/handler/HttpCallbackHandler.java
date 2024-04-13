package io.incognito.rest.client.handler;

import io.incognito.rest.client.types.dto.response.IBaseResponse;
import io.incognito.rest.client.util.Opt;
import reactor.core.publisher.SignalType;

public interface HttpCallbackHandler<RESP extends IBaseResponse> {

    /**
     * 응답 수신 시 수행할 로직
     * @param response 응답
     */
    default void onResponse(final RESP response) {
        Opt.of(response).ifPresent(resp -> {
            if (resp.isSuccess()) {
                onSuccess(resp);
            } else {
                onFailed(resp);
            }
        });
    }

    /**
     * 성공 응답 수신 시 수행할 로직
     *
     * @param response 성공 응답
     */
    void onSuccess(RESP response);

    /**
     * 실패 응답 수신 시 수행할 로직
     *
     * @param response 실패 응답
     */
    void onFailed(RESP response);

    /**
     * HTTP 요청 & 응답 처리 중 예외 발생 시 수행할 로직
     * @param t 예외
     */
    void onError(Throwable t);

    /**
     * HTTP 요청 & 응답 종료 이후 수행할 로직
     *
     * @param signalType 처리 결과 타입
     */
    void afterFinished(SignalType signalType);
}
