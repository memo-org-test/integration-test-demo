package com.document.example.commons;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class UserDto {

    private final UUID userUuid;

    private final UserType userType;
}
