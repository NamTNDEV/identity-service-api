package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.dto.response.UserResponse;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.mapper.UserMapper;
import com.namudev.identity_service.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepo userRepo;
    UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepo.findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        for (User user : users) {
            userResponseList.add(userMapper.toUserResponse(user));
        }
        return userResponseList;
    }

    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(userRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public User createUser(UserCreationRequest userRequest) {
        if(userRepo.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user =  userMapper.toUser(userRequest);
        return userRepo.save(user);
    }

    public User updateUser(String id, UserUpdateRequest userRequest) {
        User user = userRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, userRequest);
        return userRepo.save(user);
    }

    public void deleteUser(String id) {
        boolean exists = userRepo.existsById(id);
        if (!exists) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userRepo.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }
}
