package com.xjfdy.cuisine_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xjfdy.cuisine_backend.dto.userDto.AuthResponseDto;
import com.xjfdy.cuisine_backend.dto.userDto.LoginDto;
import com.xjfdy.cuisine_backend.dto.userDto.RegisterDto;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.security.JwtTokenProvider;
import com.xjfdy.cuisine_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_Success() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password");

        AuthResponseDto mockResponse = new AuthResponseDto("fake-token", 1L, "testuser", "test@email.com", "Test User");

        when(userService.login(any(LoginDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_Fail_WrongPassword() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("wrong-password");

        when(userService.login(any(LoginDto.class)))
                .thenThrow(new RuntimeException("Wrong password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong password"));
    }

    @Test
    void register_Success() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("new@test.com");
        registerDto.setPassword("pass");

        when(userService.register(any(RegisterDto.class))).thenReturn("User registered successfully");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated()) // 🌟 重点：创建成功应该是 201
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void register_Fail_UsernameTaken() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existing");

        when(userService.register(any(RegisterDto.class)))
                .thenThrow(new RuntimeException("Username is already in use"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest()) // 期待 400
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        String token = "Bearer fake-valid-token";
        String pureToken = "fake-valid-token";
        String username = "testuser";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setEmail("test@email.com");

        when(jwtTokenProvider.getUsername(pureToken)).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token)) // 模拟带 Header 请求
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void getCurrentUser_Fail_InvalidToken() throws Exception {
        String token = "Bearer invalid-token";

        when(jwtTokenProvider.getUsername(any())).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized()) // 期待 401
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}