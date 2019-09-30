package com.document.example.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Log4j2
public class AwsTestResourcesConfig {

    @Bean(destroyMethod = "shutdown")
    @Primary
    public AmazonSQS getSqsClient(@Value("${localstack.host}") String sqsHost, @Value("${localstack.tcp.4576}") String port) {
        final AmazonSQSAsync sqsClient =
                AmazonSQSAsyncClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(sqsHost + ":" + port, "cn-north-1"))
                        .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP)).build();
        return sqsClient;
    }

    @Bean(destroyMethod = "shutdown")
    @Primary
    public AmazonS3 getS3Client(@Value("${localstack.host}") String s3Host,
                                @Value("${localstack.tcp.4572}") String port,
                                @Value("${s3.userDocuments.bucketName}") String userDocumentsBucket) {
        final String signingRegion = "cn-north-1";
        final AmazonS3 s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(s3Host + ":" + port, signingRegion))
                        .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                        .withPathStyleAccessEnabled(true)
                        .disableChunkedEncoding()
                        .build();

        s3Client.createBucket(userDocumentsBucket);
        log.info("created bucket with name " + userDocumentsBucket);

        return s3Client;
    }
}
