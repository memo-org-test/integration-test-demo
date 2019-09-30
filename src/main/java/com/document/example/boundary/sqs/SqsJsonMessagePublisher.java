package com.document.example.boundary.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.function.Function;

@Log4j2
public class SqsJsonMessagePublisher<T> {

    private final String queueName;

    private final Function<T, String> messageGroupIdProvider;

    private String queueUrl;

    private String counterMetricName;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonSQS amazonSQS;

    @Autowired
    private MeterRegistry meterRegistry;

    public void publish(T messageDto) {
        log.info("process=publishSqsMessage, status=started, targetQueueName={}, value={}", queueName, messageDto);
        amazonSQS.sendMessage(getSendMessageRequest(messageDto));
        meterRegistry.counter(counterMetricName).increment();
        log.info("process=publishSqsMessage, status=success, targetQueueName={}, value={}", queueName, messageDto);
    }

    private SendMessageRequest getSendMessageRequest(T messageDto) {
        final String messageGroupId = messageGroupIdProvider.apply(messageDto);
        Assert.notNull(messageGroupId, "messageGroupId must not be null");
        final SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withMessageBody(getMessageBody(messageDto))
                .withMessageGroupId(messageGroupId)
                .withQueueUrl(queueUrl);
        return sendMessageRequest;
    }

    private String getMessageBody(T messageDto) {
        try {
            return objectMapper.writeValueAsString(messageDto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize");
        }
    }

    @Builder
    private SqsJsonMessagePublisher(String queueName, Function<T, String> messageGroupIdProvider) {
        Assert.notNull(queueName, "queueName must be provided");
        Assert.notNull(messageGroupIdProvider, "messageGroupIdProvider must be provided");
        this.queueName = queueName;
        this.messageGroupIdProvider = messageGroupIdProvider;
    }

    @PostConstruct
    void setUp() {
        queueUrl = amazonSQS.getQueueUrl(queueName).getQueueUrl();
        counterMetricName = new StringBuilder(queueName).append("_messages_published").toString();
    }
}
