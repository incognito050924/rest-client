package io.incognito.rest.client;

import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public interface IHttpRequest<AUTH> {
    WebClient getWebClient();
    HttpMethod getMethod();
    String getUrl();
    MultiValueMap<String, String> getRequestHeaders();
    MultiValueMap<String, String> getQueryParam();
    Map<String, String> getPathVariables();
    AUTH getAuthorization();
}
