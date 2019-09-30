package com.document.example.boundary.sqs.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NotificationMessageDto {

    private final UUID userId;

    private final String message;
}
