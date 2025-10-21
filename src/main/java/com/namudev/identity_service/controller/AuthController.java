package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.IntrospectRequest;
import com.namudev.identity_service.dto.request.LoginRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.dto.response.AuthResponse;
import com.namudev.identity_service.dto.response.IntrospectResponse;
import com.namudev.identity_service.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successful")
                .data(authService.authenticate(loginRequest))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) {
        boolean isValid = authService.validateToken(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder()
                .code(isValid ? 200 : 400)
                .message(isValid ? "Introspect successful" : "Introspect failed")
                .data(
                        IntrospectResponse.builder()
                                .valid(authService.validateToken(introspectRequest))
                                .build()
                )
                .build();
    }
}
