package com.namudev.identity_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = getErrorCode(authException);
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }

    private ErrorCode getErrorCode(AuthenticationException authException) {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        if(authException.getClass() == OAuth2AuthenticationException.class) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) authException;
            String errorCodeStr = oauth2Exception.getError().getErrorCode();
            errorCode = switch (errorCodeStr) {
                case "token_invalidated" -> ErrorCode.TOKEN_INVALIDATED;
                case "invalid_token" -> ErrorCode.INVALID_TOKEN;
                default -> ErrorCode.UNAUTHENTICATED;
            };
        }
        return errorCode;
    }
}
