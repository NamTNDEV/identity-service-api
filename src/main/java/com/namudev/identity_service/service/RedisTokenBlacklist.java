package com.namudev.identity_service.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisTokenBlacklist {
    StringRedisTemplate stringRedisTemplate;

    private String buildKey(String jti) {
        return "bl:jti:" + jti;
    }

    public void blacklist(String jti, Instant exp) {
        Duration ttl = (exp == null) ? Duration.ZERO : Duration.between(Instant.now(), exp);
        if(ttl.isNegative() || ttl.isZero()) return;
        String key = buildKey(jti);
        stringRedisTemplate.opsForValue().set(key, "0", ttl);
        log.info(":: Token was added to blacklist ::");
    }

    public boolean isBlacklisted(String jti) {
        return stringRedisTemplate.hasKey(buildKey(jti));
    }
}
