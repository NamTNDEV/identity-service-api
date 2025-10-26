package com.namudev.identity_service.exception;

import com.namudev.identity_service.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException exception) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(ErrorCode.UNKNOWN_ERROR.getCode());
        response.setMessage(ErrorCode.UNKNOWN_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_ERROR_CODE;
        String enumErrorCode = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        try {
            errorCode = ErrorCode.valueOf(enumErrorCode);
        } catch (IllegalArgumentException ignored) {
        }

        ConstraintViolation<?> constraintViolationException = exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
        Map<String, Object> attributes = constraintViolationException.getConstraintDescriptor().getAttributes();
        String newMessage = mappingAttributesToString(errorCode.getMessage(), attributes);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(newMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getHttpStatus()).body(
                ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    private String mappingAttributesToString(String oldMessage, Map<String, Object> attributes) {
        if(attributes == null || attributes.isEmpty()) {
            return oldMessage;
        }

        String newMessage = oldMessage;
        for (var entry : attributes.entrySet()) {
            String key = entry.getKey();
            if(key.equals("message") || key.equals("groups") || key.equals("payload")) {
                continue;
            }
            String value = entry.getValue().toString();
            newMessage = newMessage.replace("{" + key + "}", value);
        }
        return newMessage;
    }
}

