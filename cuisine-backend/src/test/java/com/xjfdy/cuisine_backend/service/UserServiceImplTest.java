package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.userDto.AuthResponseDto;
import com.xjfdy.cuisine_backend.dto.userDto.LoginDto;
import com.xjfdy.cuisine_backend.dto.userDto.RegisterDto;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.repository.UserRepository;
import com.xjfdy.cuisine_backend.security.JwtTokenProvider;
import com.xjfdy.cuisine_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    // test login() method
    // user successfully login into his/her account
    @Test
    void testLogin_Successful() {

        String username = "test1";

        // data from frontend
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail(username);
        loginDto.setPassword("123456");

        // data from database
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setFullName("Test User");
        user.setPassword("encoded123456");

        //when query "test1", return object user
        when(userRepository.findByUsername(loginDto.getUsernameOrEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword()))
                .thenReturn(true);

        when(jwtTokenProvider.generateToken(username))
                .thenReturn("fake-jwt-token-string");

        AuthResponseDto result = userService.login(loginDto);
        assertNotNull(result);

        assertEquals("fake-jwt-token-string", result.getToken()); // verify if the tokens are the same
        assertEquals("test1", result.getUsername()); // verify the username

    }

    // no such account in database
    @Test
    void testLogin_NotFound_ThrowException() {

        String notFoundUser = "test2";

        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail(notFoundUser);
        loginDto.setPassword("123456");

        when(userRepository.findByUsername(notFoundUser))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.login(loginDto));
    }

    // username exists, but user types a wrong password
    @Test
    void testLogin_WrongPassword() {
        String username = "test3";
        String inputPassword = "123456";
        String dbPassword = "111111";

        // user from frontend
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail(username);
        loginDto.setPassword(inputPassword);

        // user from repository
        User user = new User();
        user.setUsername(username);
        user.setPassword(dbPassword);

        // user from database
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(loginDto.getPassword(), user.getPassword()))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.login(loginDto));

        assertEquals("Wrong password", exception.getMessage());

    }

    // test register() method

    @Test
    void testRegister_Successful() {
        String username = "test";
        String email = "test@test.com";
        String password = "123456";

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(username);
        registerDto.setEmail(email);
        registerDto.setPassword(password);

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(false);

        when(passwordEncoder.encode(password))
                .thenReturn("encoded123456");

        String result = userService.register(registerDto);

        assertEquals("User registered successfully", result);

        verify(userRepository, times(1)).save(any(User.class));
    }

    // Username exists
    @Test
    void testRegister_UsernameAlreadyExists_ThrowException() {
        String username = "test";
        String email = "test@test.com";

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(username);
        registerDto.setEmail(email);

        when(userRepository.existsByUsername(username))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDto));

        assertEquals("Username is already in use", exception.getMessage());

        verify(userRepository, times(0)).save(any(User.class));
    }

    //email exists
    @Test
    void testRegister_EmailAlreadyExists_ThrowException() {
        String username = "test";
        String email = "test@test.com";

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername(username);
        registerDto.setEmail(email);

        when(userRepository.existsByUsername(username))
                .thenReturn(false);

        when(userRepository.existsByEmail(email))
                .thenReturn(true);


        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.register(registerDto));

        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository, never()).save(any(User.class)); // never() is similar to times(1) above
    }

    // test updateUser() method
    // update successfully
    @Test
    void testUpdateUser_Successful() {
        Long id = 5L;
        String newFullName = "newName";
        String newEmail = "new@test.com";

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setUsername("oldUser");
        existingUser.setEmail("old@test.com");
        existingUser.setFullName("Old Name");

        User updateInfo = new User();
        updateInfo.setFullName(newFullName);
        updateInfo.setEmail(newEmail);

        when(userRepository.findById(id))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmail(newEmail))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenReturn(existingUser);

        User res = userService.updateUser(id, updateInfo);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(newFullName, savedUser.getFullName());
        assertEquals(newEmail, savedUser.getEmail());
        assertEquals("oldUser", savedUser.getUsername());
    }

    @Test
    void testUpdateUser_UserNotFound_ThrowException() {
        Long nonExistentId = 999L;

        User updateInfo = new User();
        updateInfo.setFullName("Ghost");

        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(nonExistentId, updateInfo));

        assertEquals("User does not exist", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_EmailAlreadyExists_ThrowException() {
        Long id = 1L;
        String takenEmail = "taken@test.com";

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setUsername("currentUser");
        existingUser.setEmail("original@test.com");

        User updateInfo = new User();
        updateInfo.setEmail(takenEmail);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        when(userRepository.existsByEmail(takenEmail)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(id, updateInfo));

        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_Successful() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("userToDelete");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    void testDeleteUser_UserNotFound_ThrowException() {
        Long nonExistentId = 999L;

        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(nonExistentId));

        assertEquals("User does not exist", exception.getMessage());

        verify(userRepository, never()).delete(any(User.class));
    }
}
