package com.namudev.identity_service.exception;

public enum ErrorCode {
    UNKNOWN_ERROR(4999, "An unknown error occurred"),
    INVALID_ERROR_CODE(4998, "Invalid error code"),
    USER_EXISTED(4001, "Username already exists"),
    USER_NOT_FOUND(4002, "User not found"),
    INVALID_USERNAME(4003, "Username must be between 3 and 50 characters"),
    INVALID_PASSWORD(4004, "Password must be at least 8 characters long");

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
