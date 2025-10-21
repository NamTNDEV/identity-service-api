package com.namudev.identity_service.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNKNOWN_ERROR(4999, "An unknown error occurred"),
    INVALID_ERROR_CODE(4998, "Invalid error code"),
    USER_EXISTED(4001, "Username already exists"),
    USER_NOT_EXISTED(4002, "User does not exist"),
    INVALID_USERNAME(4003, "Username must be between 3 and 50 characters"),
    INVALID_PASSWORD(4004, "Password must be at least 8 characters long"),
    INVALID_CREDENTIALS(4005, "Invalid username or password"),
    USER_NOT_FOUND (4006, "User not found"),
    UNAUTHENTICATED(4007, "Unauthenticated"),;

    final int code;
    final String message;
}
