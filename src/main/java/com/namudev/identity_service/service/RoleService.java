package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.RoleRequest;
import com.namudev.identity_service.dto.response.RoleResponse;
import com.namudev.identity_service.entity.Permission;
import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.mapper.RoleMapper;
import com.namudev.identity_service.repository.RoleRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepo roleRepo;
    PermissionService permissionService;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest roleRequest) {
        if(roleRepo.existsByName(roleRequest.getName())) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTS);
        }
        Role role = roleMapper.toRole(roleRequest);
        Set<Permission> permissions = new HashSet<>(permissionService.getPermissionsByNames(roleRequest.getPermissions()));
        role.setPermissions(permissions);
        roleRepo.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepo.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void deleteRoleByName(String roleName) {
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.ROLE_NOT_FOUND);
                });
        roleRepo.delete(role);
        log.info("Role {} deleted successfully", roleName);
    }

    public Role getRoleByName(String roleName) {
        return roleRepo.findByName(roleName)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.ROLE_NOT_FOUND);
                });
    }

    public List<Role> getRolesByNames(Set<String> roleNames) {
        List<Role> roles = roleRepo.findAllById(roleNames);
        if(roles.size() != roleNames.size()) {
            log.error("One or more roles not found: {}", roleNames);
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        return roles;
    }

    public RoleResponse updateRole(String roleName, RoleRequest roleRequest) {
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.ROLE_NOT_FOUND);
                });
        Set<Permission> willAddedPermissions = new HashSet<>(permissionService.getPermissionsByNames(roleRequest.getPermissions()));
        role.setPermissions(willAddedPermissions);
        roleRepo.save(role);
        log.info("Role {} updated successfully", roleName);
        return roleMapper.toRoleResponse(role);
    }
}
