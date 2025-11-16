package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.userDto.AuthResponseDto;
import com.xjfdy.cuisine_backend.dto.userDto.LoginDto;
import com.xjfdy.cuisine_backend.dto.userDto.RegisterDto;
import com.xjfdy.cuisine_backend.entity.User;

import java.util.List;

public interface UserService {

    AuthResponseDto login(LoginDto loginDto);

    String register(RegisterDto registerDto);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User getUserById(long id);

    List<User> getAllUsers();

    User updateUser(Long id,User userDetails);

    void deleteUser(long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    String changePassword(String username, String oldPassword, String newPassword);
}
