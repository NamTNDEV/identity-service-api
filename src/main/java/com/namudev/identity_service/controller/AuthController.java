package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.IntrospectRequest;
import com.namudev.identity_service.dto.request.LoginRequest;
import com.namudev.identity_service.dto.request.RefreshTokenRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.dto.response.AuthResponse;
import com.namudev.identity_service.dto.response.IntrospectResponse;
import com.namudev.identity_service.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
        boolean isValid = authService.introspectToken(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder()
                .code(isValid ? 200 : 401)
                .message(isValid ? "Introspect successful" : "Introspect failed")
                .data(
                        IntrospectResponse.builder()
                                .valid(isValid)
                                .build()
                )
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        authService.logout(bearerToken);
        return ApiResponse.<Void>builder()
                .code(204)
                .message("Logout successful")
                .build();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Token refresh successful")
                .data(authService.refreshToken(refreshTokenRequest))
                .build();
    }

    @PostMapping("/outbound/authenticate")
    public ApiResponse<AuthResponse> outboundAuthenticate(@RequestParam("code") String code) {
        return ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Outbound authentication successful")
                .data(authService.outboundAuthenticate(code))
                .build();
    }
}
