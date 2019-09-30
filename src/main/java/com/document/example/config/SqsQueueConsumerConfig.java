package com.document.example.config;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;
import org.springframework.validation.Validator;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Configuration
@EnableSqs
@Log4j2
public class SqsQueueConsumerConfig {

    @Value("${sqs.backOffMills}")
    private Long backOffMills;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmazonSQSAsync amazonSQS;

    @Value("${sqs.userCreated.name}")
    private String userCreatedQueueName;

    @Value("${sqs.documentUploadCompleted.name}")
    private String documentUploadCompletedQueueName;

    @Value("${sqs.notification.name}")
    private String notificationQueueName;

    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory(Validator mvcValidator, QueueMessagingTemplate queueMessagingTemplate) {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        factory.setArgumentResolvers(Collections.singletonList(new PayloadArgumentResolver(queueMessagingTemplate.getMessageConverter(), mvcValidator)));
        factory.setSendToMessagingTemplate(queueMessagingTemplate);
        return factory;
    }

    private MappingJackson2MessageConverter getMessageConverter() {
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        messageConverter.setStrictContentTypeMatch(false);
        messageConverter.setObjectMapper(objectMapper);
        messageConverter.setSerializedPayloadClass(String.class);
        return messageConverter;
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(@Value("${sqs.message.batch.max}") Integer maxNumberOfMessages) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSQS);
        factory.setBackOffTime(backOffMills);
        factory.setMaxNumberOfMessages(maxNumberOfMessages);
        return factory;
    }

    @Bean
    public QueueMessagingTemplate getQueueMessagingTemplate() {
        QueueMessagingTemplate queueMessagingTemplate = new QueueMessagingTemplate(amazonSQS);
        queueMessagingTemplate.setMessageConverter(getMessageConverter());
        return queueMessagingTemplate;
    }

    @PostConstruct
    public void publishGauges() {
        meterRegistry.gauge("document_upload_completed_queue_health", isQueuePresent(documentUploadCompletedQueueName));
        meterRegistry.gauge("user_created_queue_health", isQueuePresent(userCreatedQueueName));
        meterRegistry.gauge("notification_queue_health", isQueuePresent(notificationQueueName));
    }

    private Integer isQueuePresent(String queueName) {
        try {
            amazonSQS.getQueueUrl(queueName);
            return 1;
        } catch (QueueDoesNotExistException e) {
            log.error("failed to get queue url queueName={}", queueName, e);
            return 0;
        }
    }
}
