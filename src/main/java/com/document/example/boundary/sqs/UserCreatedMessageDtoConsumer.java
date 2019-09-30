package com.document.example.boundary.sqs;

import com.document.example.boundary.sqs.dto.UserCreatedMessageDto;
import com.document.example.control.UserDocumentUploadService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

import static org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy.ON_SUCCESS;

@RequiredArgsConstructor
@Service
@Log4j2
public class UserCreatedMessageDtoConsumer {

    private final MeterRegistry meterRegistry;

    private final UserDocumentUploadService userDocumentUploadService;

    @SqsListener(value = "${sqs.userCreated.name}", deletionPolicy = ON_SUCCESS)
    @Timed(value = "user_created_message_consume")
    public void consumeUserCreatedMessageDto(@Valid UserCreatedMessageDto messageDto) {
        if (!messageDto.getUserType().isRequiredToUploadDocuments()) {
            log.info("process=consumeUserCreatedMessageDto, status=ignored, value={}", messageDto);
            return;
        }
        log.info("process=consumeUserCreatedMessageDto, status=started, value={}", messageDto);
        userDocumentUploadService.registerUserCreated(messageDto);
        meterRegistry.counter("user_created_message_consumed_count").increment();
    }

    @MessageExceptionHandler
    public void handleUserCreatedMessageDtoError(Exception e, String message) {
        meterRegistry.counter("digital_signature_account_message_consume_errors_count").increment();
        log.error("failed to process message stringValue={}", message, e);
    }
}
