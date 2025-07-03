package io.incognito.rest.client.types.dto.response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public abstract class MapResponse<K, V> extends BaseApiResponse implements Map<K, V> {
    @Delegate
    protected Map<K, V> map = new ConcurrentHashMap<>();
}
