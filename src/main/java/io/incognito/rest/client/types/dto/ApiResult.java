package io.incognito.rest.client.types.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.HttpStatus;

import io.incognito.rest.client.types.enums.ApiResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResult {
    private HttpStatus status;
    private ApiResultCode resultCode;
    private String failureMessage;
    private String failureDetail;

    public String getCode() {
        return getResultCode().getCode();
    }

    public String getMessageKo() {
        return getResultCode().getMessageKo();
    }

    public String getMessageEn() {
        return getResultCode().getMessageEn();
    }
}
