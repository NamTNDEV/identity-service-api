package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.dto.response.UserResponse;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.mapper.UserMapper;
import com.namudev.identity_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    UserService userService;
    UserMapper userMapper;

    @GetMapping("/hello-world")
    public String helloWorld() {
        return "Hello, World!";
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    ApiResponse<List<UserResponse>> getUserList() {
        List<User> result = userService.getAllUsers();
        List<UserResponse> userListResponse = result.stream().map(userMapper::toUserResponse).toList();
        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .data(userListResponse)
//                .message("User list fetched successfully.")
                .build();
    }

//    @PostAuthorize("returnObject.data.username == authentication.name")
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable String id) {
        User result = userService.getUserById(id);
        UserResponse userResponse = userMapper.toUserResponse(result);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .data(userResponse)
//                .message("User fetched successfully.")
                .build();
    }

    @PostMapping()
    ApiResponse<UserResponse> addUser(@RequestBody @Validated UserCreationRequest userRequest) {
        User result = userService.createUser(userRequest);
        UserResponse userResponse = userMapper.toUserResponse(result);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setData(userResponse);
//        response.setMessage("User created successfully.");
        response.setCode(201);
        return response;
    }

    @PutMapping("/{id}")
    ApiResponse<User> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest userRequest) {
        User result = userService.updateUser(id, userRequest);
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(result);
//        response.setMessage("User updated successfully.");
        response.setCode(200);
        return response;
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setData(null);
        response.setMessage("User deleted successfully.");
        response.setCode(200);
        return response;
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getUserInfo() {
        UserResponse userResponse = userService.getUserInfo();
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .data(userResponse)
//                .message("User info fetched successfully.")
                .build();
    }
}
