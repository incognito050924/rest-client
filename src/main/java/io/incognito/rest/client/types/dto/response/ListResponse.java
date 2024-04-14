package io.incognito.rest.client.types.dto.response;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ListResponse<E> extends BaseApiResponse implements List<E> {
    @Delegate
    protected List<E> list = new CopyOnWriteArrayList<>();
}
