package com.namudev.identity_service.controller;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
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
    List<User> getUserList(){
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    User getUserById(@PathVariable String id){
        return userService.getUserById(id);
    }

    @PostMapping()
    User addUser(@RequestBody @Validated UserCreationRequest userRequest){
        return userService.createUser(userRequest);
    }

    @PutMapping("/{id}")
    User updateUser(@PathVariable String id, @RequestBody UserUpdateRequest userRequest){
        return userService.updateUser(id, userRequest);
    }

    @DeleteMapping("/{id}")
    String deleteUser(@PathVariable String id){
        userService.deleteUser(id);
        return "User has been deleted.";
    }
}
