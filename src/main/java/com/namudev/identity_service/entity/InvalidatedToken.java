package com.namudev.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "invalidated_tokens",
        indexes = {
                @Index(name = "idx_invalidated_token_id", columnList = "expiration_date")
        }
)
public class InvalidatedToken {
    @Id
    String id;

    @Column(name = "expiration_date", nullable = false)
    Instant expirationDate;
}
