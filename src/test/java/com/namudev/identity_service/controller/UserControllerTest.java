package com.namudev.identity_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namudev.identity_service.dto.request.UserCreationRequest;
import com.namudev.identity_service.dto.response.UserResponse;
import com.namudev.identity_service.entity.User;
import com.namudev.identity_service.mapper.UserMapper;
import com.namudev.identity_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;

@Slf4j
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;
    @MockitoBean
    UserMapper userMapper;

    User user;
    UserResponse userResponse;
    UserCreationRequest userCreationRequest;

    @BeforeEach
    void initData() {
        LocalDate date = LocalDate.of(2002, 1, 3);
        user = User.builder()
                .id("user-id-123")
                .username("namudev")
                .password("password")
                .firstName("Namu")
                .lastName("Dev")
                .dob(date)
                .build();

        userCreationRequest = UserCreationRequest.builder()
                .username("namudev")
                .password("password")
                .firstName("Namu")
                .lastName("Dev")
                .dob(date)
                .build();

        userResponse = UserResponse.builder()
                .id("user-id-123")
                .username("namudev")
                .firstName("Namu")
                .lastName("Dev")
                .dob(date)
                .build();
    }

    @Test
    void addUser_validRequest_Success() throws Exception {
        Mockito.when(userService.createUser(any(UserCreationRequest.class))).thenReturn(user);
        Mockito.when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userCreationRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value("user-id-123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value("namudev"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.firstName").value("Namu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.lastName").value("Dev"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.dob").value("2002-01-03"));
    }
    @Test
    void addUser_usernameInvalid_BadRequest() throws Exception {
        UserCreationRequest invalidRequest = UserCreationRequest.builder()
                .username("") // Invalid: empty username
                .password("password")
                .firstName("Namu")
                .lastName("Dev")
                .dob(LocalDate.of(2002, 1, 3))
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(4001))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Username must be between 3 and 50 characters"));
    }
}
