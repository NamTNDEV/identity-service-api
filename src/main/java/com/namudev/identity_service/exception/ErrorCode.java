package com.namudev.identity_service.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNKNOWN_ERROR(4999, "An unknown error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_CODE(4998, "Invalid error code", HttpStatus.BAD_REQUEST),
    USER_EXISTED(4001, "Username already exists", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(4002, "User does not exist", HttpStatus.NOT_FOUND),
    INVALID_USERNAME(4003, "Username must be between 3 and 50 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(4004, "Password must be at least 8 characters long", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(4005, "Invalid username or password", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(4006, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(4007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(4008, "Unauthorized", HttpStatus.FORBIDDEN),
    PERMISSION_NOT_FOUND(4009, "Permission not found", HttpStatus.NOT_FOUND),
    PERMISSION_ALREADY_EXISTS(4010, "Permission already exists", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(4011, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(4012, "Role already exists", HttpStatus.CONFLICT);

    int code;
    String message;
    HttpStatus httpStatus;
}
