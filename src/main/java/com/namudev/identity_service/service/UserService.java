package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.dto.response.UserResponse;
import com.namudev.identity_service.entity.Role;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.mapper.UserMapper;
import com.namudev.identity_service.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.namudev.identity_service.enums.RoleEnum;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    RoleService roleService;
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
        return userRepo.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public User createUser(UserCreationRequest userRequest) {
        if(userRepo.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user =  userMapper.toUser(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(roleService.getRoleByName(RoleEnum.USER.name()));
        user.setRoles(roles);

        try {
            user = userRepo.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            log.error("Error creating user: {}", e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return user;
    }

    public User updateUser(String id, UserUpdateRequest userRequest) {
        User user = userRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, userRequest);
        if(userRequest.getRoles() != null) {
            Set<Role> roles = new HashSet<>(roleService.getRolesByNames(userRequest.getRoles()));
            user.setRoles(roles);
        }
        return userRepo.save(user);
    }

    public void deleteUser(String id) {
        boolean exists = userRepo.existsById(id);
        if (!exists) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userRepo.deleteById(id);
    }

    public UserResponse getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Username : {}", username);
        User user = getUserByUsername(username);
        return userMapper.toUserResponse(user);
    }

    public boolean isUserExist(String username) {
        return userRepo.existsByUsername(username);
    }
}
