package com.namudev.identity_service.config;

import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.repository.PermissionRepo;
import com.namudev.identity_service.repository.RoleRepo;
import com.namudev.identity_service.repository.UserRepo;
import com.namudev.identity_service.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppConfig {
    static final String ADMIN = "admin";

    PasswordEncoder passwordEncoder;
    RedisTestService redisTestService;

    @Bean
    CommandLineRunner redisPing(StringRedisTemplate srt) {
        return args -> {
            try {
                var pong = srt.execute((RedisCallback<Object>) RedisConnectionCommands::ping);
                System.out.println("Redis PING => " + pong);
//                redisTestService.testBasicOps();
            } catch (Exception e) {
                System.err.println("❌ Redis connect failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        };
    }

    @Bean
    public ApplicationRunner applicationRunner(UserRepo userRepo, RoleRepo roleRepo, PermissionRepo permissionRepo) {
        return args -> {
            if (userRepo.findByUsername(ADMIN).isPresent()) {
                log.warn("User with name {} already exists", ADMIN);
                return;
            }
            var allPermissions = permissionRepo.findAll();
            var adminRole = roleRepo.findByName("ADMIN").map(
                    r -> {
                        if (r.getPermissions() == null) {
                            r.setPermissions(new HashSet<>(allPermissions));
                            return roleRepo.save(r);
                        }
                        return r;
                    }).orElseGet(
                    () -> roleRepo.save(
                            Role.builder()
                                    .name("ADMIN")
                                    .description("Administrator role with all permissions")
                                    .permissions(new HashSet<>(allPermissions))
                                    .build()
                    )
            );

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);


            var userAdmin = User.builder()
                    .username(ADMIN)
                    .password(passwordEncoder.encode(ADMIN))
                    .dob(LocalDate.of(2000, 1, 1))
                    .roles(roles)
                    .build();
            userRepo.save(userAdmin);
            log.info("Admin user created with username: {} and password: {}", ADMIN, ADMIN);
        };
    }
}
