package io.incognito.rest.client.types.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import io.incognito.rest.client.types.enums.ApiResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResult {
    private HttpStatus status;
    private MultiValueMap<String, String> responseHeaders;
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
