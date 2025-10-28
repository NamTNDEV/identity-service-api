package com.namudev.identity_service.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    /* =========================
     * 40xx – Validation / Input
     * ========================= */
    INVALID_USERNAME(4001, "Username must be between {min} and {max} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4002, "Password must be at least {min} characters long", HttpStatus.BAD_REQUEST),
    INVALID_DOB(4003, "Date of birth indicates user is under {min}", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(4004, "Invalid username or password", HttpStatus.BAD_REQUEST),
    MALFORMED_TOKEN(4005, "Malformed or unreadable token", HttpStatus.BAD_REQUEST),
    INVALID_ERROR_CODE(4006, "Invalid error code", HttpStatus.BAD_REQUEST),

    /* =========================
     * 41xx – AuthN / AuthZ state
     * ========================= */
    UNAUTHENTICATED(4101, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(4102, "Unauthorized", HttpStatus.FORBIDDEN),

    /* =========================
     * 42xx – Token/JWT issues
     * ========================= */
    INVALID_TOKEN(4201, "Invalid token", HttpStatus.UNAUTHORIZED),
    INVALID_SIGNATURE(4202, "Invalid token signature", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(4203, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_ISSUER(4204, "Invalid token issuer", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALIDATED(4205, "Token has been invalidated", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN_TYPE(4206, "Invalid token type", HttpStatus.UNAUTHORIZED),
    INVALID_AUTH_HEADER(4207, "Invalid Authorization header", HttpStatus.UNAUTHORIZED),

    /* =========================
     * 43xx – Domain/Resource state
     * ========================= */
    USER_EXISTED(4301, "Username already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(4302, "User not found", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(4303, "Permission not found", HttpStatus.NOT_FOUND),
    PERMISSION_ALREADY_EXISTS(4304, "Permission already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(4304, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(4305, "Role already exists", HttpStatus.CONFLICT),
    INVALIDATED_TOKEN_NOT_FOUND(4306, "Invalidated token not found", HttpStatus.NOT_FOUND),

    /* =========================
     * 49xx – System/Unknown
     * ========================= */
    UNKNOWN_ERROR(4999, "An unknown error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    int code;
    String message;
    HttpStatus httpStatus;
}
