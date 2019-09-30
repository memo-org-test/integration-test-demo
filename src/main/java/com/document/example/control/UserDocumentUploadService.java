package com.document.example.control;

import com.document.example.boundary.sqs.SqsJsonMessagePublisher;
import com.document.example.boundary.sqs.dto.NotificationMessageDto;
import com.document.example.boundary.sqs.dto.UserCreatedMessageDto;
import com.document.example.boundary.userinfo.UserInfoHttpClient;
import com.document.example.commons.DocumentState;
import com.document.example.control.dto.DocumentUploadDto;
import com.document.example.control.dto.UserDocumentDto;
import com.document.example.control.dto.UserDocumentUploadTaskDto;
import com.document.example.control.exceptions.InvalidDocumentIdException;
import com.document.example.entity.model.UserDocument;
import com.document.example.entity.repository.UserDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class UserDocumentUploadService {

    private final UserDocumentRepository userDocumentRepository;

    private final SqsJsonMessagePublisher<NotificationMessageDto> notificationMessageDtoPublisher;

    private final UserDocumentsS3Service userDocumentsS3Service;

    private final UserInfoHttpClient userInfoHttpClient;

    public void registerUserCreated(UserCreatedMessageDto userCreatedMessageDto) {
        Assert.notNull(userCreatedMessageDto, "userCreatedMessageDto must not be null");
        if (!userInfoHttpClient.getUserInfo(userCreatedMessageDto.getUserUuid()).getAddressCountryCode().equals("US")) {
            return;
        }
        userDocumentRepository.saveAll(createUserDocuments(userCreatedMessageDto));

        notificationMessageDtoPublisher.publish(NotificationMessageDto.builder()
                .userId(userCreatedMessageDto.getUserUuid())
                .message("you have requested documents to be uploaded")
                .build());
    }

    public List<UserDocumentUploadTaskDto> getUploadTasks(UUID userUuid) {
        Assert.notNull(userUuid, "userUuid must not be null");
        return userDocumentRepository.findAllByUserIdAndState(userUuid, DocumentState.REQUESTED).stream()
                .map(UserDocumentUploadTaskDto::of)
                .collect(Collectors.toList());
    }

    public void uploadDocument(DocumentUploadDto documentUploadDto) throws IOException {
        Assert.notNull(documentUploadDto, "documentUploadDto must not be null");
        final UserDocument userDocument = getUserRequestedDocument(documentUploadDto);

        userDocumentsS3Service.uploadToS3(UserDocumentDto.of(userDocument), documentUploadDto.getDocumentInputStream());
        userDocumentRepository.save(userDocument.withState(DocumentState.UPLOADED));
    }

    private UserDocument getUserRequestedDocument(DocumentUploadDto documentUploadDto) {
        return userDocumentRepository.findById(documentUploadDto.getDocumentId())
                .filter(document -> document.getUserId().equals(documentUploadDto.getUserId()))
                .filter(document -> document.getState() == DocumentState.REQUESTED)
                .orElseThrow(() -> new InvalidDocumentIdException("document already uploaded or found"));
    }

    private static List<UserDocument> createUserDocuments(UserCreatedMessageDto userCreatedMessageDto) {
        return userCreatedMessageDto.getUserType().getRequiredDocuments().stream()
                .map(documentType -> UserDocument.builder()
                        .documentType(documentType)
                        .userId(userCreatedMessageDto.getUserUuid())
                        .build())
                .collect(Collectors.toList());
    }
}
