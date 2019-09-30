package com.document.example.config;

import com.document.example.boundary.sqs.SqsJsonMessagePublisher;
import com.document.example.boundary.sqs.dto.DocumentUploadCompletedMessageDto;
import com.document.example.boundary.sqs.dto.NotificationMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Log4j2
public class SqsPublisherConfig {

    @Bean
    public SqsJsonMessagePublisher<DocumentUploadCompletedMessageDto> DocumentUploadCompletedMessageDtoPublisher(
            @Value("${sqs.documentUploadCompleted.name}") String documentUploadCompletedQueueName) {
        return SqsJsonMessagePublisher.<DocumentUploadCompletedMessageDto>builder()
                .queueName(documentUploadCompletedQueueName)
                .messageGroupIdProvider(message -> message.getUserUuid().toString())
                .build();
    }

    @Bean
    public SqsJsonMessagePublisher<NotificationMessageDto> notificationMessagePublisher(
            @Value("${sqs.notification.name}") String notificationQueueName) {
        return SqsJsonMessagePublisher.<NotificationMessageDto>builder()
                .queueName(notificationQueueName)
                .messageGroupIdProvider(message -> message.getUserId().toString())
                .build();
    }
}
