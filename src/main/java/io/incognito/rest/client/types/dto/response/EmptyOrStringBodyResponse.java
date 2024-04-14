package io.incognito.rest.client.types.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmptyOrStringBodyResponse extends BaseApiResponse implements IBaseResponse {
    private String bodyString;
}
