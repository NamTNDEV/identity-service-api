package com.namudev.identity_service.config;

import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.repository.PermissionRepo;
import com.namudev.identity_service.repository.RoleRepo;
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
    public ApplicationRunner applicationRunner(UserRepo userRepo, RoleRepo roleRepo, PermissionRepo permissionRepo) {
        return args -> {
            if (userRepo.findByUsername(ADMIN).isPresent()) {
                log.warn("User with name {} already exists", ADMIN);
                return;
            }

            var allPermissions = permissionRepo.findAll();
            var adminRole = roleRepo.findByName("ADMIN").map(
                    r -> {
                        if (r.getPermissions() == null || r.getPermissions().isEmpty()) {
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

            var userAdmin = User.builder()
                    .username(ADMIN)
                    .password(passwordEncoder.encode(ADMIN))
                    .roles(Set.of(adminRole))
                    .build();

            userRepo.save(userAdmin);
            log.info("Admin user created with username: {} and password: {}", ADMIN, ADMIN);
        };
    }
}
