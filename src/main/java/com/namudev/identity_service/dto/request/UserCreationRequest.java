package com.namudev.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 3, max = 50, message = "INVALID_USERNAME")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    String firstName;

    String lastName;

    LocalDate dob;

    Set<String> roles;
}
