package com.document.example.control;

import com.document.example.boundary.sqs.SqsJsonMessagePublisher;
import com.document.example.boundary.sqs.dto.DocumentUploadCompletedMessageDto;
import com.document.example.boundary.sqs.dto.NotificationMessageDto;
import com.document.example.control.exceptions.InvalidDocumentIdException;
import com.document.example.commons.DocumentState;
import com.document.example.control.dto.S3UrlAccess;
import com.document.example.control.dto.UserDocumentDto;
import com.document.example.entity.model.UserDocument;
import com.document.example.entity.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentVerificationTaskService {

    private final UserDocumentRepository userDocumentRepository;

    private final UserDocumentsS3Service userDocumentsS3Service;

    private final SqsJsonMessagePublisher<NotificationMessageDto> notificationMessageDtoPublisher;

    private final SqsJsonMessagePublisher<DocumentUploadCompletedMessageDto> documentUploadCompletedMessagePublisher;

    public List<S3UrlAccess<UserDocumentDto>> getUnVerifiedDocuments() {
        return userDocumentRepository.findAllByState(DocumentState.UPLOADED).stream()
                .map(UserDocumentDto::of)
                .map(userDocumentsS3Service::wrapWithS3Access)
                .collect(Collectors.toList());
    }

    public void verifyDocument(UUID documentId, boolean accepted) {
        Assert.notNull(documentId, "documentId must not be null");
        final UserDocument userDocument = userDocumentRepository.findById(documentId)
                .orElseThrow(() -> new InvalidDocumentIdException("document not found with id" + documentId));
        if (accepted) {
            userDocument.setState(DocumentState.VERIFIED);
            publishIfAllRequiredDocumentsVerified(userDocument);
            return;
        }
        userDocument.setState(DocumentState.REQUESTED);
        userDocumentRepository.save(userDocument);

        notifyUser(userDocument.getUserId());
    }

    private void publishIfAllRequiredDocumentsVerified(UserDocument userDocument) {
        final List<UserDocument> allByUserId = userDocumentRepository.findAllByUserId(userDocument.getUserId());
        if (allByUserId.stream().allMatch(document -> document.getState() == DocumentState.VERIFIED)) {
            documentUploadCompletedMessagePublisher.publish(DocumentUploadCompletedMessageDto.builder()
                    .userUuid(userDocument.getUserId())
                    .documentTypes(allByUserId.stream().map(UserDocument::getDocumentType).collect(Collectors.toList()))
                    .build());
        }
    }

    private void notifyUser(UUID userId) {
        notificationMessageDtoPublisher.publish(NotificationMessageDto.builder()
                .userId(userId)
                .message("a document you have submitted was rejected")
                .build());
    }
}
