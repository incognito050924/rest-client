package io.incognito.rest.client.types.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApiResultCode {
    SUCCESS("0000", "성공", "Success"),
    INVALID_PARAMETER("1000", "파라미터 오류", "Invalid parameter"),
    INVALID_RESPONSE("1002", "응답 오류", "Invalid response"),
    INVALID_AUTH("1003", "인증 오류", "Invalid authentication"),
    INVALID_SYSTEM("1004", "시스템 오류", "System Error"),
    INVALID_NETWORK("1005", "네트워크 오류", "Network Error"),
    FAILED_TO_SERIALIZE("1006", "타입 변환 오류 (Serialization)", "Failed to serialization"),
    FAILED_TO_DESERIALIZE("1007", "타입 변환 오류 (Deserialization)", "Failed to deserialization"),
    INVALID_ETC("1099", "기타 오류", "Unknown Error"),
    UNKNOWN_STATUS("9999", "알 수 없는 상태", "Invalid API Status");

    private final String code;
    private final String messageKo;
    private final String messageEn;

    public static ApiResultCode fromHttpStatus(final HttpStatus status) {
        if (status != null) {
            if (status.is2xxSuccessful() || status.is3xxRedirection() || status.is1xxInformational()) {
                return SUCCESS;
            } else if (status.equals(HttpStatus.UNAUTHORIZED) || status.equals(HttpStatus.FORBIDDEN)) {
                return INVALID_AUTH;
            } else if (status.equals(HttpStatus.BAD_REQUEST)) {
                return INVALID_PARAMETER;
            } else if (status.is4xxClientError() || status.is5xxServerError()) {
                return INVALID_RESPONSE;
            }
        }
        return UNKNOWN_STATUS;
    }
}
