package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.dto.response.ApiResponse;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    ApiResponse<List<User>> getUserList(){
        List<User> result = userService.getAllUsers();
        ApiResponse<List<User>> response = new ApiResponse<>();
        response.setData(result);
//        response.setMessage("User list fetched successfully.");
        response.setCode(200);
        return response;
    }

    @GetMapping("/{id}")
    ApiResponse<User> getUserById(@PathVariable String id){
        User result = userService.getUserById(id);
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(result);
//        response.setMessage("User fetched successfully.");
        response.setCode(200);

        return response;
    }

    @PostMapping()
    ApiResponse<User> addUser(@RequestBody @Validated UserCreationRequest userRequest){
        User result = userService.createUser(userRequest);
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(result);
//        response.setMessage("User created successfully.");
        response.setCode(201);
        return response;
    }

    @PutMapping("/{id}")
    ApiResponse<User> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest userRequest){
        User result = userService.updateUser(id, userRequest);
        ApiResponse<User> response = new ApiResponse<>();
        response.setData(result);
//        response.setMessage("User updated successfully.");
        response.setCode(200);
        return response;
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteUser(@PathVariable String id){
        userService.deleteUser(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.setData(null);
        response.setMessage("User deleted successfully.");
        response.setCode(200);
        return response;
    }
}
