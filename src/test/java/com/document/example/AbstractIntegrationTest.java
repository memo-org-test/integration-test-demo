package com.document.example;

import com.amazonaws.services.s3.AmazonS3;
import com.document.example.entity.repository.UserDocumentRepository;
import com.document.example.utils.HttpRequestHelper;
import com.document.example.utils.MockServerRequestHelper;
import com.document.example.utils.SqsTestHelper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Tag("integration-test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    protected SqsTestHelper sqsTestHelper;

    @Autowired
    protected AmazonS3 amazonS3;

    @Autowired
    protected MockServerRequestHelper mockServerRequestHelper;

    @Autowired
    protected UserDocumentRepository userDocumentRepository;

    @Autowired
    protected HttpRequestHelper httpRequestHelper;

    @BeforeAll
    public void setupAppPort() {
        RestAssured.port = serverPort;
    }

    @BeforeEach
    void reset() {
        mockServerRequestHelper.reset();
        sqsTestHelper.purgeQueues();
        userDocumentRepository.deleteAll();
    }
}
