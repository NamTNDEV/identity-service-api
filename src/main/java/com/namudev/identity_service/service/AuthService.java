package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.IntrospectRequest;
import com.namudev.identity_service.dto.request.LoginRequest;
import com.namudev.identity_service.dto.response.AuthResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    UserService userService;
    @NonFinal
    @Value("${jwt.secret-key}")
    protected String SECRET_KEY;

    private String buildScopeString(Set<Role> roles) {
        StringJoiner joiner = new StringJoiner("");
        if (!CollectionUtils.isEmpty(roles)) {
            roles.forEach(role -> {
                joiner.add("ROLE_" + role.getName() + " ");
                if (!role.getPermissions().isEmpty()) {
                    role.getPermissions().forEach(permission -> {
                        joiner.add(permission.getName() + " ");
                    });
                }
            });
            return joiner.toString().trim();
        } else {
            return "";
        }
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("com.namudev.identity_service")
                .issueTime(new Date())
                .claim("scope", buildScopeString(user.getRoles()))
                .expirationTime(
                        new Date(System.currentTimeMillis() + 3600 * 1000) // 1 hour expiration
                )
                .build();
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            System.out.println("Error signing the token: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean validateToken(IntrospectRequest introspectRequest) {
        try {
            String token = introspectRequest.getToken();
            JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isVerified = signedJWT.verify(verifier);
            return isVerified && expirationTime.after(new Date());
        } catch (Exception e) {
            System.out.println("Error validating the token: " + e.getMessage());
            return false;
        }
    }

    ;

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

    ;
}
