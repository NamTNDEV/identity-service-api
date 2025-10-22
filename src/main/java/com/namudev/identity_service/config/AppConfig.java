package com.namudev.identity_service.config;

import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.enums.Role;
import com.namudev.identity_service.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppConfig {
    PasswordEncoder passwordEncoder;
    static final String ADMIN = "admin";

    @Bean
    public ApplicationRunner applicationRunner(UserRepo userRepo) {
        return args -> {
            if(userRepo.findByUsername(ADMIN).isEmpty()) {
                Set<String> roles = new HashSet<>();
                roles.add(Role.ADMIN.name());
                var userAdmin = User.builder()
                        .username(ADMIN)
                        .password(passwordEncoder.encode(ADMIN))
                        .roles(roles)
                        .build();

                userRepo.save(userAdmin);
                log.info("Admin user created with username: {} and password: {}", ADMIN, ADMIN);
            } else {
                log.info("Admin user already exists");
            }
        };
    }
}
