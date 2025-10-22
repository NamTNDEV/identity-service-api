package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.enums.Role;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.mapper.UserMapper;
import com.namudev.identity_service.repository.UserRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepo userRepo;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(String id) {
        return userRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public User createUser(UserCreationRequest userRequest) {
        if(userRepo.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user =  userMapper.toUser(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);
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
}
