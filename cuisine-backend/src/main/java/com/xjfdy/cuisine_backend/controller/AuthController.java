package com.xjfdy.cuisine_backend.controller;

import com.xjfdy.cuisine_backend.dto.userDto.AuthResponseDto;
import com.xjfdy.cuisine_backend.dto.userDto.LoginDto;
import com.xjfdy.cuisine_backend.dto.userDto.MessageResponseDto;
import com.xjfdy.cuisine_backend.dto.userDto.RegisterDto;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.security.JwtTokenProvider;
import com.xjfdy.cuisine_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            AuthResponseDto response = userService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDto(e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto registerDto) {
        try {
            String message = userService.register(registerDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponseDto(message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDto(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String username = jwtTokenProvider.getUsername(jwt);
            User user = userService.getUserByUsername(username);

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDto("Invalid token"));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            boolean isValid = jwtTokenProvider.validateToken(jwt);

            if (isValid) {
                return ResponseEntity.ok(new MessageResponseDto("Valid token"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponseDto("Invalid token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDto("Invalid token"));
        }
    }

}
