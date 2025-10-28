package com.namudev.identity_service.service;

import com.namudev.identity_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class RedisTestService {
    StringRedisTemplate stringRedisTemplate;
    RedisTemplate<String, Object> redisTemplate;

    public void testBasicOps() {
        // Test-case 1: string key-value
        if (!stringRedisTemplate.hasKey("hello")) {
            stringRedisTemplate.opsForValue().set("hello", "Redis_updated");
        }

        String value = stringRedisTemplate.opsForValue().get("hello");
        log.info("💬 Redis says: {}", value);

        // Test-case 2: object storage
        if(!redisTemplate.hasKey("user:1")) {
            User user = User.builder()
                    .username("redis_user_updated")
                    .firstName("Redis")
                    .lastName("User")
                    .build();

            redisTemplate.opsForValue().set("user:1", user);
        }

        User retrievedUser = (User) redisTemplate.opsForValue().get("user:1");
        log.info("👤 Fetched user: {}", retrievedUser);

        // Test-case 3: list
        String listKey = "myList";
        if (!redisTemplate.hasKey(listKey)) {
            List<Integer> integerList = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                integerList.add(i);
            }
            redisTemplate.opsForList().rightPushAll(listKey, integerList);
        }

        List<Object> fetchedList = redisTemplate.opsForList().range(listKey, 0, -1);
        log.info("📜 Fetched list: {}", fetchedList);

        // Test-case 4: set
        String mapKey = "myMap";
        if(!redisTemplate.hasKey(mapKey)) {
            Map<String, String> sampleMap = Map.of(
                    "field1", "value1",
                    "field2", "value2",
                    "field3", "value3"
            );
            redisTemplate.opsForHash().putAll(mapKey, sampleMap);
        }

        Map<Object, Object> fetchedMap = redisTemplate.opsForHash().entries(mapKey);
        log.info("🗺️ Fetched map: {}", fetchedMap);
    }
}
