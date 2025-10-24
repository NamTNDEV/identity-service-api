package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.PermissionRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.dto.response.PermissionResponse;
import com.namudev.identity_service.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> addPermission(@RequestBody PermissionRequest permissionRequest) {
      return ApiResponse.<PermissionResponse>builder()
              .code(201)
              .message("Permission added successfully")
              .data(
                      permissionService.create(permissionRequest)
              )
              .build();
    };

    @GetMapping
    ApiResponse<List<PermissionResponse>> getAllPermissions(String name) {
      return ApiResponse.<List<PermissionResponse>>builder()
              .code(200)
              .message("Permission fetched successfully")
              .data(permissionService.getAll())
              .build();
    };

    @GetMapping("/{name}")
    ApiResponse<PermissionResponse> getPermissionByName(@PathVariable String name) {
        PermissionResponse permissionResponse = permissionService.getByName(name);
        return ApiResponse.<PermissionResponse>builder()
                .code(200)
                .data(permissionResponse)
                .message("Permission fetched successfully.")
                .build();
    }

    @DeleteMapping("/{name}")
    ApiResponse<?> deletePermissionByName(@PathVariable String name) {
        permissionService.deleteByName(name);
        return ApiResponse.builder()
                .code(200)
                .message("Permission deleted successfully.")
                .build();
    }
}
