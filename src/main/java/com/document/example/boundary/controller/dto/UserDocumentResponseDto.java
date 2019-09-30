package com.document.example.boundary.controller.dto;

import com.document.example.commons.DocumentState;
import com.document.example.entity.model.DocumentType;
import com.document.example.control.dto.S3UrlAccess;
import com.document.example.control.dto.UserDocumentDto;
import lombok.Builder;
import lombok.Getter;

import java.net.URL;
import java.util.UUID;

@Builder
@Getter
public class UserDocumentResponseDto {

    private final UUID userId;

    private final UUID documentId;

    private final DocumentState documentState;

    private final DocumentType documentType;

    private final URL s3PreSignedUrl;

    public static UserDocumentResponseDto of(S3UrlAccess<UserDocumentDto> userDocumentDtoS3UrlAccess) {
        return UserDocumentResponseDto.builder()
                .userId(userDocumentDtoS3UrlAccess.getValue().getUserId())
                .documentId(userDocumentDtoS3UrlAccess.getValue().getDocumentId())
                .documentState(userDocumentDtoS3UrlAccess.getValue().getDocumentState())
                .documentType(userDocumentDtoS3UrlAccess.getValue().getDocumentType())
                .s3PreSignedUrl(userDocumentDtoS3UrlAccess.getPreSignedUrl())
                .build();
    }
}
