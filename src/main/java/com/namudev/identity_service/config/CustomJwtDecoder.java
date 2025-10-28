package com.namudev.identity_service.config;

import com.namudev.identity_service.service.RedisTokenBlacklist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@Slf4j
public record CustomJwtDecoder(
        JwtDecoder jwtDecoder,
        RedisTokenBlacklist redisTokenBlacklist
) implements JwtDecoder {
    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt;

        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            log.error("Jwt decode error::", e);
            throw e;
        }

        String jti = jwt.getId();
        if (jti != null && redisTokenBlacklist.isBlacklisted(jti)) {
            log.error("Invalidated token used with jti: {}", jti);
            OAuth2Error error = new OAuth2Error(
                    "token_invalidated",
                    "Token has been invalidated",
                    null
            );
            throw new OAuth2AuthenticationException(error);
        }
        return jwt;
    }
}
