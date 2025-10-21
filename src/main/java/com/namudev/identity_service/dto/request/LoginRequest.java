package com.namudev.identity_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "INVALID_CREDENTIALS")
    private String username;

    @NotBlank(message = "INVALID_CREDENTIALS")
    private String password;
}
