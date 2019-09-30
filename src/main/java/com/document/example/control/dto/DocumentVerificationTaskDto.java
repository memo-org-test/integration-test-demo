package com.document.example.control.dto;

import com.document.example.entity.model.DocumentType;
import com.document.example.entity.model.UserDocument;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DocumentVerificationTaskDto {

    private final UUID documentId;

    private final DocumentType documentType;

    private final UUID userId;

    public static DocumentVerificationTaskDto of(UserDocument userDocument) {
        return DocumentVerificationTaskDto.builder()
                .documentId(userDocument.getId())
                .documentType(userDocument.getDocumentType())
                .userId(userDocument.getUserId())
                .build();
    }
}
