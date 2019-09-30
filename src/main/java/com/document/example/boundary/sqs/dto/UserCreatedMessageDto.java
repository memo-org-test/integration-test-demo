package com.document.example.boundary.sqs.dto;

import com.document.example.commons.UserType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@ToString
public class UserCreatedMessageDto {

    @NotNull
    private UUID userUuid;

    @NotNull
    private Instant createAt;

    @NotNull
    private UserType userType;
}
