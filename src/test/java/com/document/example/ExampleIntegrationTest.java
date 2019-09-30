package com.document.example;

import com.document.example.boundary.controller.dto.UploadDocumentRequestDto;
import com.document.example.boundary.sqs.dto.DocumentUploadCompletedMessageDto;
import com.document.example.boundary.sqs.dto.NotificationMessageDto;
import com.document.example.boundary.sqs.dto.UserCreatedMessageDto;
import com.document.example.boundary.userinfo.dto.UserInfoResponseDto;
import com.document.example.commons.UserType;
import com.document.example.control.dto.UserDocumentUploadTaskDto;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.document.example.utils.HttpRequestHelper.operatorUser;
import static com.document.example.utils.HttpRequestHelper.tenantUser;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class ExampleIntegrationTest extends AbstractIntegrationTest {

    private UserCreatedMessageDto testUserCreatedMessage;

    @Value("classpath:test-document.pdf")
    private Resource testDocumentPdf;

    @BeforeEach
    void setup() {
        testUserCreatedMessage = UserCreatedMessageDto.builder()
                .createAt(Instant.now())
                .userUuid(UUID.randomUUID())
                .build();

        mockServerRequestHelper.givenResponseToGetUserInfo(testUserCreatedMessage.getUserUuid(), UserInfoResponseDto.builder()
                .addressCountryCode("US")
                .build());
    }

    @AfterEach
    void cleanup() {
        mockServerRequestHelper.reset();
        sqsTestHelper.purgeQueues();
    }

    @Test
    void whenATENANTUserCreatedMessageIsConsumed_UserUploadsRequiredDocuments_OperatorAcceptedThem_ShouldPublishDocumentUploadCompletedMessageDto() throws IOException {
        testUserCreatedMessage = testUserCreatedMessage.toBuilder().userType(UserType.TENANT).build();

        final List<UUID> userDocumentUploadIds = givenUploadTasksOnUserCreation();
        givenUserSuccessFullyUploadedRequiredDocuments(userDocumentUploadIds);
        givenOperatorAcceptedAllDocuments(userDocumentUploadIds);

        final List<DocumentUploadCompletedMessageDto> documentUploadCompletedMessageDto = sqsTestHelper.retrieveDocumentUploadCompletedMessageDto();
        assertThat(documentUploadCompletedMessageDto).hasSize(1);
        assertThat(documentUploadCompletedMessageDto.get(0).getDocumentTypes()).containsAll(testUserCreatedMessage.getUserType().getRequiredDocuments());
        assertThat(documentUploadCompletedMessageDto.get(0).getUserUuid()).isEqualTo(testUserCreatedMessage.getUserUuid());
    }

    @Test
    void whenATENANTUserCreatedMessageIsPublished_andUserCountryCodeNotUS_shouldIgnoreMessageAnNotTaskShouldIsFound() {
        testUserCreatedMessage = testUserCreatedMessage.toBuilder().userType(UserType.TENANT).build();
        mockServerRequestHelper.givenResponseToGetUserInfo(testUserCreatedMessage.getUserUuid(), UserInfoResponseDto.builder()
                .addressCountryCode("NotUs")
                .build());

        httpRequestHelper.whenGetUploadTasksForUser(tenantUser(testUserCreatedMessage.getUserUuid()))
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private void givenOperatorAcceptedAllDocuments(List<UUID> userDocumentUploadIds) {
        userDocumentUploadIds.forEach(documentId ->
                httpRequestHelper.whenPutVerifyDocumentAccepted(operatorUser(UUID.randomUUID()), documentId).then()
                        .statusCode(HttpStatus.ACCEPTED.value()));
    }

    private void givenUserSuccessFullyUploadedRequiredDocuments(List<UUID> documentIds) throws IOException {
        final UploadDocumentRequestDto uploadDocumentRequestDto = new UploadDocumentRequestDto(
                Base64.getEncoder().encodeToString(ByteStreams.toByteArray(testDocumentPdf.getInputStream())));

        documentIds.forEach(documentId -> {
            httpRequestHelper.whenPutUploadRequest(tenantUser(testUserCreatedMessage.getUserUuid()), documentId, uploadDocumentRequestDto)
                    .then()
                    .statusCode(HttpStatus.ACCEPTED.value());
        });
    }

    private List<UUID> givenUploadTasksOnUserCreation() {
        sqsTestHelper.givenAPublishedUserCreatedMessageAwaitedToBeConsumed(testUserCreatedMessage);

        final UserDocumentUploadTaskDto[] response = httpRequestHelper.whenGetUploadTasksForUser(tenantUser(testUserCreatedMessage.getUserUuid()))
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().body().as(UserDocumentUploadTaskDto[].class);

        final List<NotificationMessageDto> notificationMessageDtos = sqsTestHelper.retrieveNotificationMessages();
        assertThat(notificationMessageDtos).hasSize(1);
        assertThat(notificationMessageDtos.get(0).getUserId()).isEqualTo(testUserCreatedMessage.getUserUuid());

        assertThat(response).containsExactlyInAnyOrderElementsOf(buildExpectedResponse(testUserCreatedMessage));
        return Arrays.asList(response).stream().map(UserDocumentUploadTaskDto::getDocumentId).collect(Collectors.toList());
    }

    private List<UserDocumentUploadTaskDto> buildExpectedResponse(UserCreatedMessageDto testUser) {
        return userDocumentRepository.findAllByUserId(testUser.getUserUuid()).stream()
                .map(UserDocumentUploadTaskDto::of)
                .collect(Collectors.toList());
    }
}
