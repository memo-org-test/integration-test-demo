package com.document.example.boundary.userinfo;

import com.document.example.boundary.userinfo.dto.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(url = "${userInfoService.host}", name = "userInfo")
public interface UserInfoHttpClient {

    @GetMapping(path = "/user/{userId}/info", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    UserInfoResponseDto getUserInfo(@PathVariable UUID userId);
}
