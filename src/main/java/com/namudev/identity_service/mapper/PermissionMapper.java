package com.namudev.identity_service.mapper;

import com.namudev.identity_service.dto.request.PermissionRequest;
import com.namudev.identity_service.dto.response.PermissionResponse;
import com.namudev.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);
    PermissionResponse toPermissionResponse(Permission permission);
}
