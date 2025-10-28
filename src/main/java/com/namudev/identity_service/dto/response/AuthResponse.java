package com.namudev.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AuthResponse {
    String accessToken;
    String refreshToken;
}
