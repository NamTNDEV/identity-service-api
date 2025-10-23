package com.namudev.identity_service.repository;

import com.namudev.identity_service.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, String> {
    Optional<Permission> findByName(String name);
}
