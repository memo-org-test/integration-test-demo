package com.document.example.control.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Builder
@Getter
public class DocumentUploadDto implements Closeable {

    private final InputStream documentInputStream;

    private final UUID documentId;

    private final UUID userId;

    @Override
    public void close() throws IOException {
        documentInputStream.close();
    }
}
