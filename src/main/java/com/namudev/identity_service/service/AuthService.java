package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.IntrospectRequest;
import com.namudev.identity_service.dto.request.LoginRequest;
import com.namudev.identity_service.dto.request.LogoutRequest;
import com.namudev.identity_service.dto.response.AuthResponse;
import com.namudev.identity_service.entity.InvalidatedToken;
import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    public static String ISSUER = "com.namudev.identity_service";
    static Duration TOKEN_TTL = Duration.ofHours(1);

    @NonFinal
    @Value("${jwt.secret-key}")
    String SECRET_KEY_B64;

    UserService userService;
    InvalidatedTokenService invalidatedTokenService;

    private String buildScopeString(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(" ");
        roles.forEach(role -> {
            joiner.add("ROLE_" + role.getName());
            var permissions = role.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                permissions.forEach(permission -> joiner.add(permission.getName()));
            }
        });
        return joiner.toString();
    }

    private String generateToken(User user) {
        var now = Instant.now();
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScopeString(user.getRoles()))
                .expirationTime(Date.from(now.plus(TOKEN_TTL)))
                .build();
        // Cách 1:
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        // Cách 2:
        // SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet);

        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET_KEY_B64);
            jwsObject.sign(new MACSigner(secretKeyBytes));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error signing token", e);
        }
    }

    private SignedJWT verifyToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 1. Enforce algorithm
            var alg = signedJWT.getHeader().getAlgorithm();
            if (!alg.equals(JWSAlgorithm.HS512)) {
                log.error("Invalid JWT algorithm: {}", alg);
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            // 2. Verify signature
            byte[] secretKeyBytes = Base64.getDecoder().decode(SECRET_KEY_B64);
            JWSVerifier verifier = new MACVerifier(secretKeyBytes);
            boolean sigOk = signedJWT.verify(verifier);
            if (!sigOk) {
                log.error("Token signature verification failed");
                throw new AppException(ErrorCode.INVALID_SIGNATURE);
            }

            // 3. Validate standard claims
            var claims = signedJWT.getJWTClaimsSet();

            // 3.a. Expiration time
            var now = Instant.now();
            Instant expDate = claims.getExpirationTime().toInstant();
            if (expDate != null && now.isAfter(expDate)) {
                log.error("Token expired at {}", expDate);
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            // 3.b. Issuer
            String issuer = claims.getIssuer();
            if (issuer != null && !ISSUER.equals(issuer)) {
                log.error("Invalid issuer: {}", issuer);
                throw new AppException(ErrorCode.INVALID_TOKEN_ISSUER);
            }

            // 4. Check if token is invalidated
            String jti = claims.getJWTID();
            Optional<InvalidatedToken> invalidatedTokenOpt = invalidatedTokenService.getInvalidatedTokenById(jti);
            if(invalidatedTokenOpt.isPresent()) {
                log.error("Token has been invalidated: {}", jti);
                throw new AppException(ErrorCode.TOKEN_INVALIDATED);
            }

            return signedJWT;
        }
        // List exceptions have to caught: [ParseException, JOSEException] + others: [AppException, Exception]
        catch (ParseException e) {
            log.error("Malformed token: {}", e.getMessage());
            throw new AppException(ErrorCode.MALFORMED_TOKEN);
        } catch (JOSEException e) {
            log.error("Error verifying token: {}", e.getMessage());
            throw new AppException(ErrorCode.INVALID_SIGNATURE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            throw new AppException(ErrorCode.UNKNOWN_ERROR);
        }
    }

    public boolean introspectToken(IntrospectRequest introspectRequest) {
        String token = introspectRequest.getToken();
        verifyToken(token);
        return true;
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        User user = userService.getUserByUsername(loginRequest.getUsername());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean isPasswordMatching = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!isPasswordMatching) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String token = generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest logoutRequest) {
        log.info("::: Processing logout for token ::");
        String token = logoutRequest.getToken();
        SignedJWT signedJWT = verifyToken(token);

        try {
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            var expDate = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

            invalidatedTokenService.createInvalidatedToken(jti, expDate);
        } catch (ParseException e) {
            log.error("Invalid JWT ID: {}", e.getMessage());
            throw new AppException(ErrorCode.MALFORMED_TOKEN);
        }

    }
}
