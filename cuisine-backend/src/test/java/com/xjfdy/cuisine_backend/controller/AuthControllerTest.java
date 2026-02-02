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

@WebMvcTest(AuthController.class) // 1. 只测试 AuthController
@AutoConfigureMockMvc(addFilters = false) // 2. 暂时关掉 Security 过滤器，防止测试被 403 拦截
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // 模拟发请求的工具 (就像代码版的 Postman)

    @MockitoBean // 3. 模拟 Service，不要真的去连数据库
    private UserService userService;

    @MockitoBean // 模拟 JwtTokenProvider
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper; // 用于把对象转成 JSON 字符串

    // --- 1. 测试登录成功 (200 OK) ---
    @Test
    void login_Success() throws Exception {
        // Arrange
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password");

        AuthResponseDto mockResponse = new AuthResponseDto("fake-token", 1L, "testuser", "test@email.com", "Test User");

        // 告诉 Mockito：当调用 service.login 时，返回成功的 response
        when(userService.login(any(LoginDto.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto))) // 把 DTO 转成 JSON 发送
                .andExpect(status().isOk()) // 期待 200
                // {"token":"fake-token","type":"Bearer","id":1,"username":"testuser",...}
                // 因为json的字段名就叫token，所以jsonPath不能用accessToken
                .andExpect(jsonPath("$.token").value("fake-token")) // 验证返回的 JSON 里有 token
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // --- 2. 测试登录失败 (401 Unauthorized) ---
    @Test
    void login_Fail_WrongPassword() throws Exception {
        // Arrange
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("wrong-password");

        // 模拟 Service 抛出异常 (模仿 UserServiceImpl 里的逻辑)
        when(userService.login(any(LoginDto.class)))
                .thenThrow(new RuntimeException("Wrong password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized()) // 期待 401
                .andExpect(jsonPath("$.message").value("Wrong password")); // 验证错误信息
    }

    // --- 3. 测试注册成功 (201 Created) ---
    @Test
    void register_Success() throws Exception {
        // Arrange
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("newuser");
        registerDto.setEmail("new@test.com");
        registerDto.setPassword("pass");

        when(userService.register(any(RegisterDto.class))).thenReturn("User registered successfully");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated()) // 🌟 重点：创建成功应该是 201
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    // --- 4. 测试注册失败 - 用户名已存在 (400 Bad Request) ---
    @Test
    void register_Fail_UsernameTaken() throws Exception {
        // Arrange
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("existing");

        when(userService.register(any(RegisterDto.class)))
                .thenThrow(new RuntimeException("Username is already in use"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest()) // 期待 400
                .andExpect(jsonPath("$.message").value("Username is already in use"));
    }

    // --- 5. 测试获取当前用户 /me (200 OK) ---
    @Test
    void getCurrentUser_Success() throws Exception {
        // Arrange
        String token = "Bearer fake-valid-token";
        String pureToken = "fake-valid-token";
        String username = "testuser";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setEmail("test@email.com");

        // 模拟解析 Token 的过程
        when(jwtTokenProvider.getUsername(pureToken)).thenReturn(username);
        when(userService.getUserByUsername(username)).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token)) // 模拟带 Header 请求
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    // --- 6. 测试获取当前用户 - Token 无效 (401 Unauthorized) ---
    @Test
    void getCurrentUser_Fail_InvalidToken() throws Exception {
        // Arrange
        String token = "Bearer invalid-token";

        // 模拟 Token 解析报错
        when(jwtTokenProvider.getUsername(any())).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized()) // 期待 401
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }
}