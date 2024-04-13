package io.incognito.rest.client.types.dto.request;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

import lombok.Getter;

@Getter
public class MultipartFormDataRequest {
    private final MultipartBodyBuilder builder = new MultipartBodyBuilder();

    public MultipartFormDataRequest addPart(final String name, final Object value) {
        builder.part(name, value);
        return this;
    }

    public MultipartFormDataRequest addPart(final String name, final Path filePath) {
        builder.part(name, new FileSystemResource(filePath));
        return this;
    }

    public MultipartFormDataRequest addPart(final String name, final File file) {
        builder.part(name, new FileSystemResource(file));
        return this;
    }

    public MultipartFormDataRequest addPart(final String name, final MultipartFile file) {
        builder.part(name, file);
        return this;
    }

    public MultiValueMap<String, HttpEntity<?>> toMultiValueMap() {
        return builder.build();
    }
}
