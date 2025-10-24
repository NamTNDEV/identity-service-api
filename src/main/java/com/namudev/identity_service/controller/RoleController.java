package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.RoleRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.dto.response.RoleResponse;
import com.namudev.identity_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest roleRequest) {
        log.info("Create role endpoint called");
        return ApiResponse.<RoleResponse>builder()
                .code(201)
                .message("Role created successfully")
                .data(
                        roleService.create(roleRequest)
                )
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('' + T(com.namudev.identity_service.enums.RoleEnum).ADMIN.name())")
    public ApiResponse<List<RoleResponse>> getListRole() {
        log.info("Get all roles endpoint called");
        return ApiResponse.<List<RoleResponse>>builder()
                .code(200)
                .message("Roles retrieved successfully")
                .data(
                        roleService.getAllRoles()
                )
                .build();
    }

    @DeleteMapping("/{name}")
    public ApiResponse<Void> deleteRole(@PathVariable("name") String roleName) {
        log.info("Delete role endpoint called for role: {}", roleName);
        roleService.deleteRoleByName(roleName);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Role deleted successfully")
                .build();
    }

    @PutMapping("/{name}")
    public ApiResponse<RoleResponse> updateRole(@PathVariable("name") String roleName, @RequestBody RoleRequest roleRequest) {
        log.info("Update role endpoint called for role: {}", roleName);
        return ApiResponse.<RoleResponse>builder()
                .code(200)
                .message("Role updated successfully")
                .data(
                        roleService.updateRole(roleName, roleRequest)
                )
                .build();
    }
}
