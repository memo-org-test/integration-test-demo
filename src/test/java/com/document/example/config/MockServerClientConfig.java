package com.document.example.config;

import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockServerClientConfig {

    @Bean
    public MockServerClient setup(@Value("${mockServer.tcp.1080}") Integer mockServerPort,
                                  @Value("${mockServer.host}") String  host) {
        return new MockServerClient(host, mockServerPort);
    }
}
