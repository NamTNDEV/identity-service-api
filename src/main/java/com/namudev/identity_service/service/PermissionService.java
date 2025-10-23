package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.PermissionRequest;
import com.namudev.identity_service.dto.response.PermissionResponse;
import com.namudev.identity_service.entity.Permission;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.mapper.PermissionMapper;
import com.namudev.identity_service.repository.PermissionRepo;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionService {
    PermissionRepo permissionRepo;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest permissionRequest) {
        if(permissionRepo.findByName(permissionRequest.getName()).isPresent()){
            log.error("Permission with name {} already exists", permissionRequest.getName());
            throw new AppException(ErrorCode.PERMISSION_ALREADY_EXISTS);
        }
        return permissionMapper.toPermissionResponse(
                permissionRepo.save(
                        permissionMapper.toPermission(permissionRequest)
                )
        );
    }

    public PermissionResponse getByName(String name) {
        Permission permission = permissionRepo.findByName(name)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepo.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void deleteByName(String name) {
        Permission permission = permissionRepo.findByName(name)
                .orElseThrow(() -> {
                    return new AppException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        permissionRepo.delete(permission);
    }

    public List<Permission> getPermissionsByNames(Set<String> names) {
        return permissionRepo.findAllById(names);
    }
}
