package com.document.example.control.dto;

import com.document.example.commons.DocumentState;
import com.document.example.entity.model.DocumentType;
import com.document.example.entity.model.UserDocument;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UserDocumentDto {

    private final UUID userId;

    private final UUID documentId;

    private final DocumentType documentType;

    private final DocumentState documentState;

    public static UserDocumentDto of(UserDocument userDocument) {
        return UserDocumentDto.builder()
                .userId(userDocument.getUserId())
                .documentId(userDocument.getId())
                .documentState(userDocument.getState())
                .documentType(userDocument.getDocumentType())
                .build();
    }
}
