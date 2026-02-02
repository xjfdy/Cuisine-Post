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

        // --- Act (执行) ---
        String result = userService.register(registerDto);

        // --- Assert (验证返回值) ---
        assertEquals("User registered successfully", result);

        // --- Verify (验证行为) ---
        // 验证: userRepository.save() 被调用了一次，且参数是任意 User 类
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

        //成功找到老用户
        when(userRepository.findById(id))
                .thenReturn(Optional.of(existingUser));

        //模拟新邮箱是否有人占用
        when(userRepository.existsByEmail(newEmail))
                .thenReturn(false);

        //模拟save()
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
        // --- 1. Arrange (准备道具) ---
        Long nonExistentId = 999L;

        // 随便准备一个 User 对象充当参数，反正进去第一行就报错，这个对象里的数据根本没机会被用到
        User updateInfo = new User();
        updateInfo.setFullName("Ghost");

        // 🎭 导演安排：
        // 当 Service 去查 ID 为 999 的人时，Repository 递给它一个“空盒子” (Optional.empty)
        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // --- 2. Act & Assert (开拍 & 验收) ---
        // 验证：调用方法时必须抛出 RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(nonExistentId, updateInfo));

        // 验证：报错口供必须一致
        assertEquals("User does not exist", exception.getMessage());

        // --- 3. Verify (防守验证) ---
        // 验证：Repository 的 save 方法绝对没有被调用过
        // (防止代码逻辑写错，没找到人还瞎保存空对象)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser_EmailAlreadyExists_ThrowException() {
        // --- 1. Arrange (准备) ---
        Long id = 1L;
        String takenEmail = "taken@test.com";

        // A. 准备数据库里的“老用户” (注意：他的邮箱必须和要改的邮箱不一样)
        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setUsername("currentUser");
        existingUser.setEmail("original@test.com"); // 旧邮箱

        // B. 准备想改成的“新数据”
        User updateInfo = new User();
        updateInfo.setEmail(takenEmail); // 想改成这个已被占用的邮箱

        // C. Mock (导演剧本)
        // 1. 先让 Service 找到当前用户 (不然第一步就挂了)
        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        // 2. 关键冲突点：告诉 Service 这个新邮箱已经有人用了
        when(userRepository.existsByEmail(takenEmail)).thenReturn(true);

        // --- 2. Act & Assert (执行 & 验收) ---
        // 验证：必须抛出 RuntimeException
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(id, updateInfo));

        // 验证：错误信息必须准确
        assertEquals("Email is already in use", exception.getMessage());

        // --- 3. Verify (防守验证) ---
        // 验证：save 方法绝对没被调用 (防止脏数据入库)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUser_Successful() {
        // --- 1. Arrange (准备) ---
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setUsername("userToDelete");

        // Mock: 先查到这个人
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // --- 2. Act (执行) ---
        userService.deleteUser(userId);

        // --- 3. Verify (验证) ---
        // 关键：验证 delete 方法被调用了，而且删的就是我们查出来的那个 existingUser
        verify(userRepository, times(1)).delete(existingUser);
    }

    @Test
    void testDeleteUser_UserNotFound_ThrowException() {
        // --- 1. Arrange (准备) ---
        Long nonExistentId = 999L;

        // Mock: 查不到人
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // --- 2. Act & Assert (执行 & 验证异常) ---
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteUser(nonExistentId));

        assertEquals("User does not exist", exception.getMessage());

        // --- 3. Verify (防守验证) ---
        // 关键：因为没找到人，所以 delete 方法绝对不能被执行
        verify(userRepository, never()).delete(any(User.class));
    }
}
