package io.incognito.rest.client.types.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmptyOrStringBodyResponse extends BaseApiResponse implements IBaseResponse {
    private String bodyString;
}
