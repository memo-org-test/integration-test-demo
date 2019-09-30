package com.document.example.control.dto;

import lombok.Builder;
import lombok.Getter;

import java.net.URL;

@Builder
@Getter
public class S3UrlAccess<T> {

    private final T value;

    private final URL preSignedUrl;
}
