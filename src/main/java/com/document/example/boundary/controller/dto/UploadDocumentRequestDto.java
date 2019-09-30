package com.document.example.boundary.controller.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.document.example.control.dto.DocumentUploadDto;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@Getter
public class UploadDocumentRequestDto {

    @NotNull
    private final String base64EncodedDocument;

    public DocumentUploadDto toUserDocumentUploadTaskDto(UUID userId, UUID documentId) {
        return DocumentUploadDto.builder()
                .userId(userId)
                .documentId(documentId)
                .documentInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64EncodedDocument)))
                .build();
    }
}
