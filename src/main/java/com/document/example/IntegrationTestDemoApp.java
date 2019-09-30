package com.document.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class IntegrationTestDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(IntegrationTestDemoApp.class, args);
    }

}
