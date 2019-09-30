package com.document.example.utils;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.document.example.boundary.sqs.dto.DocumentUploadCompletedMessageDto;
import com.document.example.boundary.sqs.dto.NotificationMessageDto;
import com.document.example.boundary.sqs.dto.UserCreatedMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.awaitility.core.ConditionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;

@Component
@Log4j2
public class SqsTestHelper {

    @Value("${sqs.notification.name}")
    private String notificationQueueName;

    @Value("${sqs.documentUploadCompleted.name}")
    private String documentUploadCompletedQueueName;

    @Value("${sqs.userCreated.name}")
    private String userCreatedQueueName;

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private ObjectMapper objectMapper;

    private String userCreatedQueueUrl;

    private String notificationQueueUrl;

    private String documentUploadCompletedQueueUrl;

    public void givenAPublishedUserCreatedMessageAwaitedToBeConsumed(UserCreatedMessageDto userCreatedMessageDto) {
        publishMessageToQueue(userCreatedMessageDto, userCreatedQueueUrl);
        log.info("waiting for message to be consumed! from queue={}", userCreatedMessageDto);
        awaitAtMost2Second().until(() -> queueHasNoMessagesInFlight(userCreatedQueueUrl));
    }

    private void publishMessageToQueue(Object message, String queueUrl) {
        String messageBody = serialize(message);
        amazonSQS.sendMessage(new SendMessageRequest(queueUrl, messageBody).withMessageGroupId(UUID.randomUUID().toString()));
        log.info("published message to url={} payload={}", queueUrl, messageBody);
    }

    private Boolean queueHasNoMessagesInFlight(String queueUrl) {
        GetQueueAttributesResult all = amazonSQS.getQueueAttributes(new GetQueueAttributesRequest().withAttributeNames("All").withQueueUrl(queueUrl));
        log.info("checking if message to be removed from the queue");
        return Integer.valueOf(all.getAttributes().get("ApproximateNumberOfMessagesNotVisible")) == 0;
    }

    public static ConditionFactory awaitAtMost2Second() {
        return await().pollInterval(300, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS);
    }

    public List<DocumentUploadCompletedMessageDto> retrieveDocumentUploadCompletedMessageDto() {
        return receiveFromQueue(documentUploadCompletedQueueUrl, DocumentUploadCompletedMessageDto.class);
    }

    public List<NotificationMessageDto> retrieveNotificationMessages() {
        return receiveFromQueue(notificationQueueUrl, NotificationMessageDto.class);
    }

    public void purgeQueues() {
        amazonSQS.purgeQueue(new PurgeQueueRequest().withQueueUrl(notificationQueueUrl));
        amazonSQS.purgeQueue(new PurgeQueueRequest().withQueueUrl(documentUploadCompletedQueueUrl));
        amazonSQS.purgeQueue(new PurgeQueueRequest().withQueueUrl(userCreatedQueueUrl));
    }

    private <T> List<T> receiveFromQueue(String documentUploadCompletedQueueUrl, Class<T> clazz) {
        return amazonSQS.receiveMessage(documentUploadCompletedQueueUrl).getMessages()
                .stream()
                .map(Message::getBody)
                .map(messageBody -> deserialize(messageBody, clazz)).collect(Collectors.toList());
    }

    private <T> T deserialize(String body, Class<T> clazz) {
        try {
            return objectMapper.readValue(body, clazz);
        } catch (IOException e) {
            throw new RuntimeException("failed to deserialize string value=" + body, e);
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to deserialize object", e);
        }
    }

    private String createQueue(String notificationQueueName) {
        HashMap<String, String> configMap = new HashMap<>();
        configMap.put("VisibilityTimeout", "1");
        configMap.put("ContentBasedDeduplication", "true");
        configMap.put("FifoQueue", "true");

        return amazonSQS.createQueue(new CreateQueueRequest().withQueueName(notificationQueueName).withAttributes(configMap)).getQueueUrl();
    }

    @PostConstruct
    void setup() {
        notificationQueueUrl = createQueue(notificationQueueName);
        documentUploadCompletedQueueUrl = createQueue(documentUploadCompletedQueueName);
        userCreatedQueueUrl = createQueue(userCreatedQueueName);
    }
}
