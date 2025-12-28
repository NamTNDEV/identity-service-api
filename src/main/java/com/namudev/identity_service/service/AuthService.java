package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.*;
import com.namudev.identity_service.dto.response.AuthResponse;
import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.enums.TokenType;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.repository.http_client.OutboundIdentityClient;
import com.namudev.identity_service.repository.http_client.OutboundUserClient;
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
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    @NonFinal
    @Value("${jwt.issuer}")
    String ISSUER;

    @NonFinal
    @Value("${jwt.access.ttl}")
    Duration ACCESS_TOKEN_TTL;

    @NonFinal
    @Value("${jwt.access.secret-key}")
    String ACCESS_SECRET_KEY_B64;

    @NonFinal
    @Value("${jwt.refresh.ttl}")
    Duration REFRESH_TOKEN_TTL;

    @NonFinal
    @Value("${jwt.refresh.secret-key}")
    String REFRESH_SECRET_KEY_B64;

    @NonFinal
    @Value("${google.oauth2.client_secret}")
    String CLIENT_SECRET;

    @NonFinal
    @Value("${google.oauth2.client_id}")
    String CLIENT_ID;

    @NonFinal
    @Value("${google.oauth2.redirect_uri}")
    String REDIRECT_URI;

    @NonFinal
    @Value("${google.oauth2.grant_type}")
    String GRANT_TYPE;

    UserService userService;
    RedisTokenBlacklist tokenBlacklist;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;

    private String buildScopeString(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(" ");
        roles.forEach(role -> {
            joiner.add("ROLE_" + role.getName());
            var permissions = role.getPermissions();
            if (permissions != null) {
                permissions.forEach(permission -> joiner.add(permission.getName()));
            }
        });
        return joiner.toString();
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer(ISSUER).issueTime(Date.from(now)).expirationTime(Date.from(now.plus(ACCESS_TOKEN_TTL))).jwtID(UUID.randomUUID().toString()).claim("scope", buildScopeString(user.getRoles())).claim("typ", TokenType.ACCESS.name()).build();
        return signHS512Token(claims, ACCESS_SECRET_KEY_B64);
    }

    private String generateRefreshToken(User user) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(user.getUsername()).issuer(ISSUER).issueTime(Date.from(now)).expirationTime(Date.from(now.plus(REFRESH_TOKEN_TTL))).jwtID(UUID.randomUUID().toString()).claim("typ", TokenType.REFRESH.name()).build();
        return signHS512Token(claims, REFRESH_SECRET_KEY_B64);
    }

    private String signHS512Token(JWTClaimsSet claims, String secretKeyB64) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        SignedJWT jwt = new SignedJWT(jwsHeader, claims);
        try {
            byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyB64);
            jwt.sign(new MACSigner(secretKeyBytes));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, String secretKeyB64, TokenType expectedType) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 1. Algorithm
            var alg = signedJWT.getHeader().getAlgorithm();

            if (!JWSAlgorithm.HS512.equals(alg)) {
                log.error(":: Invalid JWT algorithm ::");
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            // 2. Signature
            byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyB64);
            JWSVerifier verifier = new MACVerifier(secretKeyBytes);
            boolean sigOk = signedJWT.verify(verifier);
            if (!sigOk) {
                log.error(":: Invalid token signature ::");
                throw new AppException(ErrorCode.INVALID_SIGNATURE);
            }

            // 3. Validate standard claims
            var claims = signedJWT.getJWTClaimsSet();

            // 3.a. Expiration time
            Instant expDate = claims.getExpirationTime().toInstant();
            if (expDate != null && Instant.now().isAfter(expDate)) {
                log.error(":: Token expired at {} ::", expDate);
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            // 3.b. Issuer
            String issuer = claims.getIssuer();
            if (issuer != null && !ISSUER.equals(issuer)) {
                log.error(":: Invalid issuer: {} ::", issuer);
                throw new AppException(ErrorCode.INVALID_TOKEN_ISSUER);
            }

            // 3.c. Enforce typ
            String type = claims.getStringClaim("typ");
            if (!expectedType.name().equals(type)) {
                log.error(":: Invalid token type: {} ::", type);
                throw new AppException(ErrorCode.INVALID_TOKEN_TYPE);
            }

            // 4. Check if token is invalidated
            String jti = claims.getJWTID();
            if (tokenBlacklist.isBlacklisted(jti)) {
                log.error(":: Token has been invalidated ::");
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

    private SignedJWT verifyAccessToken(String token) {
        return verifyToken(token, ACCESS_SECRET_KEY_B64, TokenType.ACCESS);
    }

    private SignedJWT verifyRefreshToken(String token) {
        return verifyToken(token, REFRESH_SECRET_KEY_B64, TokenType.REFRESH);
    }

    private String extractBearer(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            log.error("Invalid Authorization header format");
            throw new AppException(ErrorCode.INVALID_AUTH_HEADER);
        }
        return bearerToken.substring(7);
    }

    public boolean introspectToken(IntrospectRequest introspectRequest) {
        log.debug(":: Processing token introspection ::");
        String token = introspectRequest.getToken();
        verifyAccessToken(token);
        return true;
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        User user = userService.getUserByUsername(loginRequest.getUsername());
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean isPasswordMatching = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        if (!isPasswordMatching) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    public void logout(String bearerToken) {
        log.info("::: Processing logout for token ::");
        String token = extractBearer(bearerToken);
        SignedJWT signedJWT = verifyAccessToken(token);

        try {
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            var expDate = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();

            if (tokenBlacklist.isBlacklisted(jti)) {
                log.warn("Token already blacklisted: {}", jti);
                return;
            }
            tokenBlacklist.blacklist(jti, expDate);
        } catch (ParseException e) {
            log.error("Invalid JWT ID: {}", e.getMessage());
            throw new AppException(ErrorCode.MALFORMED_TOKEN);
        }
    }

    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("::: Processing refresh token for token ::");
        try {
            SignedJWT signedJWT = verifyRefreshToken(refreshTokenRequest.getToken());
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            var expDate = signedJWT.getJWTClaimsSet().getExpirationTime().toInstant();
            tokenBlacklist.blacklist(jti, expDate);

            String username = signedJWT.getJWTClaimsSet().getSubject();
            User existedUser = userService.getUserByUsername(username);
            if (existedUser == null) {
                log.error("Invalid username for token :: {}", username);
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            String accessToken = generateAccessToken(existedUser);
            String refreshToken = generateRefreshToken(existedUser);

            return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
        } catch (ParseException e) {
            log.error("Invalid JWT Subject: {}", e.getMessage());
            throw new AppException(ErrorCode.MALFORMED_TOKEN);
        }
    }

    public AuthResponse outboundAuthenticate(String code) {
        log.info("::: Processing outbound authentication for code :: {}", code);

        var response = outboundIdentityClient.exchangeToken(
                ExchangeTokenRequest.builder()
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URI)
                        .clientId(CLIENT_ID)
                        .grantType(GRANT_TYPE)
                        .code(code)
                        .build()
        );

        var userInfo = outboundUserClient.getUserInfo(
                response.getAccessToken(),
                "json"
        );

        User onBoardedUser;

        if(!userService.isUserExist(userInfo.getEmail())) {
            onBoardedUser = userService.createUser(
                    UserCreationRequest.builder()
                            .username(userInfo.getEmail())
                            .password(UUID.randomUUID().toString()) // Random password since we don't use it
                            .lastName(userInfo.getFamilyName())
                            .firstName(userInfo.getGivenName())
                            .dob(LocalDate.of(1970, 1, 1)) // Default DOB
                            .build()
            );
        } else {
            onBoardedUser = userService.getUserByUsername(userInfo.getEmail());
        }

        String accessToken = generateAccessToken(onBoardedUser);
        String refreshToken = generateRefreshToken(onBoardedUser);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
