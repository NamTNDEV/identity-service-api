package com.namudev.identity_service.repository.http_client;

import com.namudev.identity_service.dto.response.GetUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth2/v1/userinfo")
    GetUserInfoResponse getUserInfo(
            @RequestParam("access_token") String accessToken,
            @RequestParam("alt") String alt
    );
}
