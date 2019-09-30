package com.document.example.boundary.sqs.dto;

import com.document.example.entity.model.DocumentType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
public class DocumentUploadCompletedMessageDto {

    private final UUID userUuid;

    private final List<DocumentType> documentTypes;
}
