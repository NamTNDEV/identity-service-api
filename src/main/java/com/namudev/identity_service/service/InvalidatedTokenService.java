package com.namudev.identity_service.service;

import com.namudev.identity_service.entity.InvalidatedToken;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.repository.InvalidatedTokenRepo;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InvalidatedTokenService {
    InvalidatedTokenRepo invalidatedTokenRepo;

    public void createInvalidatedToken(String jti, Instant expiration) {
        invalidatedTokenRepo.save(
            InvalidatedToken.builder()
                .id(jti)
                .expirationDate(expiration)
                .build()
        );
    }

    public Optional<InvalidatedToken> getInvalidatedTokenById(String id) {
        return invalidatedTokenRepo.findById(id);
    }

    public boolean isInvalidated(String jti) {
        return invalidatedTokenRepo.existsById(jti);
    }
}
