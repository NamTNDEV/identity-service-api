package com.namudev.identity_service.service;

import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.request.UserUpdateRequest;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.exception.AppException;
import com.namudev.identity_service.exception.ErrorCode;
import com.namudev.identity_service.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public User getUserById(String id) {
        return userRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public User createUser(UserCreationRequest userRequest) {
        if(userRepo.existsByUsername(userRequest.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = new User();

        user.setUsername(userRequest.getUsername());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setPassword(userRequest.getPassword());
        user.setDob(userRequest.getDob());

        return userRepo.save(user);
    }

    public User updateUser(String id, UserUpdateRequest userRequest) {
        User user = getUserById(id);

        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setDob(userRequest.getDob());

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
