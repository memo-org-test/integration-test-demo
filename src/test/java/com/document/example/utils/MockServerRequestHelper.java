package com.document.example.utils;

import com.amazonaws.HttpMethod;
import com.document.example.boundary.userinfo.dto.UserInfoResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static org.mockserver.model.JsonBody.json;

@Component
@RequiredArgsConstructor
public class MockServerRequestHelper {

    private static final Header jsonHeader = new Header("Content-Type", "application/json");

    public static final String USER_INFO = "/user/%s/info";

    private final MockServerClient mockServerClient;

    private final ObjectMapper objectMapper;

    public void givenResponseToGetUserInfo(UUID userUuid, UserInfoResponseDto userInfoResponseDto) {
        mockServerClient.when(HttpRequest.request()
                .withPath(String.format(USER_INFO, userUuid.toString()))
                .withMethod(HttpMethod.GET.name()), Times.exactly(1))
                .respond(jsonResponse()
                        .withStatusCode(200)
                        .withBody(json(userInfoResponseDto)));
    }

    public void reset() {
        mockServerClient.reset();
    }

    private HttpResponse jsonResponse() {
        return HttpResponse.response().withHeader(jsonHeader);
    }

    private <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to deserialize string to object", e);
        }
    }
}
