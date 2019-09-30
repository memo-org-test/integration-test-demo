package com.document.example.boundary.userinfo.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor(onConstructor_ = {@JsonCreator})
@Getter
public class UserInfoResponseDto {

    private final String addressCountryCode;
}
