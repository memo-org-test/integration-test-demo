package com.document.example.utils;

import com.document.example.boundary.controller.dto.UploadDocumentRequestDto;
import com.document.example.commons.UserType;
import com.document.example.commons.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

@Component
@RequiredArgsConstructor
public class HttpRequestHelper {

    public static final String DOCUMENTS_TASKS = "/documents/upload/tasks/";
    public static final String DOCUMENTS_UPLOAD = "/documents/";
    public static final String DOCUMENTS_VERIFY_ACCEPT = "/verify/documents/%s/accept";

    private final ObjectMapper objectMapper;

    public Response whenGetUploadTasksForUser(UserPrincipal userPrincipal) {
        return givenAnAuthorizedUser(userPrincipal).get(DOCUMENTS_TASKS);
    }

    public Response whenPutUploadRequest(UserPrincipal userPrincipal, UUID documentId, UploadDocumentRequestDto uploadDocumentRequestDto) {
        return givenAnAuthorizedUser(userPrincipal)
                .with()
                .body(toJson(uploadDocumentRequestDto))
                .put(DOCUMENTS_UPLOAD + documentId.toString());
    }

    public Response whenPutVerifyDocumentAccepted(UserPrincipal operatorUser, UUID documentId) {
        return givenAnAuthorizedUser(operatorUser).put(String.format(DOCUMENTS_VERIFY_ACCEPT, documentId.toString()));
    }

    private String toJson(UploadDocumentRequestDto uploadDocumentRequestDto) {
        try {
            return objectMapper.writeValueAsString(uploadDocumentRequestDto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize object", e);
        }
    }

    private RequestSpecification givenAnAuthorizedUser(UserPrincipal userPrincipal) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header(new Header("X-userId", userPrincipal.getUserId().toString()))
                .header(new Header("X-roles", userPrincipal.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","))));
    }

    public static UserPrincipal tenantUser(UUID userId) {
        return createUser(userId, UserType.TENANT);
    }

    public static UserPrincipal staffUser(UUID userId) {
        return createUser(userId, UserType.STAFF);
    }

    public static UserPrincipal operatorUser(UUID userId) {
        return createUser(userId, UserType.OPERATOR);
    }

    private static UserPrincipal createUser(UUID userId, UserType authority) {
        return UserPrincipal.builder()
                .userId(userId)
                .authorities(Sets.newHashSet(new SimpleGrantedAuthority(authority.name())))
                .build();
    }
}
