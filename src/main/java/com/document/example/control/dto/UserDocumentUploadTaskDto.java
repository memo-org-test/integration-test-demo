package com.document.example.control.dto;

import com.document.example.entity.model.DocumentType;
import com.document.example.entity.model.UserDocument;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class UserDocumentUploadTaskDto {

    private final UUID documentId;

    private final DocumentType documentType;

    public static UserDocumentUploadTaskDto of(UserDocument userDocument) {
        return UserDocumentUploadTaskDto.builder()
                .documentId(userDocument.getId())
                .documentType(userDocument.getDocumentType())
                .build();
    }
}
