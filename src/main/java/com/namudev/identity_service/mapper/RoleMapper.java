package com.namudev.identity_service.mapper;

import com.namudev.identity_service.dto.request.RoleRequest;
import com.namudev.identity_service.dto.response.RoleResponse;
import com.namudev.identity_service.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);
    RoleResponse toRoleResponse(Role role);
}
