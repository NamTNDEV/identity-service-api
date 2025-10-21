package com.namudev.identity_service.repository;

import com.namudev.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);
}
