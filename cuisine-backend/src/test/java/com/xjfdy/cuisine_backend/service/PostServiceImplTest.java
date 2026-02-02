package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.entity.Post;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.exception.NotFoundException;
import com.xjfdy.cuisine_backend.repository.PostRepository;
import com.xjfdy.cuisine_backend.repository.UserRepository;
import com.xjfdy.cuisine_backend.service.impl.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void testCreatePost_Success() {
        // 1. Arrange
        String username = "testUser";
        PostDto inputDto = new PostDto();
        inputDto.setTitle("New Title");
        inputDto.setBody("New Body");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);

        // 模拟查人成功
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // 模拟保存成功 (注意：这里我们要让 save 方法返回一个带 ID 的 Post)
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setUserId(100L); // 模拟数据库生成了 ID
            return p;
        });

        // 2. Act
        PostDto result = postService.createPost(inputDto, username);

        // 3. Assert
        assertNotNull(result);
        assertEquals("New Title", result.getTitle());

        // 验证：是否把查出来的 User 设置给了 Post？
        verify(postRepository).save(argThat(post ->
                post.getUser().getUsername().equals(username)
        ));
    }

    @Test
    void testCreatePost_UserNotFound_ThrowException() {
        // --- 1. Arrange ---
        String nonExistentUser = "ghost";
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Ghost Post");

        // Mock: 查无此人
        when(userRepository.findByUsername(nonExistentUser))
                .thenReturn(Optional.empty());

        // --- 2. Act & Assert ---
        // 验证：必须抛出 NotFoundException
        assertThrows(NotFoundException.class, () -> {
            postService.createPost(inputDto, nonExistentUser);
        });

        // --- 3. Verify ---
        // 关键防守：因为人没找到，绝对不能去调用保存方法！
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testCreatePost_WithImage_Success() {
        // --- 1. Arrange ---
        String username = "photographer";
        String validUrl = "http://example.com/image.jpg";

        PostDto inputDto = new PostDto();
        inputDto.setTitle("Photo Post");
        inputDto.setBody("Look at this!");
        inputDto.setImageUrl(validUrl); // 👈 重点测试这个字段

        User mockUser = new User();
        mockUser.setUsername(username);

        // Mock
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // 模拟 Save: 简单的返回原对象即可，因为我们只查字段映射
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setUserId(200L);
            return p;
        });

        // --- 2. Act ---
        PostDto result = postService.createPost(inputDto, username);

        // --- 3. Assert ---
        assertNotNull(result);
        assertEquals(validUrl, result.getImageUrl()); // 验证返回值里有图

        // Verify: 验证存进数据库的那个对象，ImageUrl 确实是对的
        verify(postRepository).save(argThat(post ->
                post.getImageUrl().equals(validUrl) &&
                        post.getTitle().equals("Photo Post") &&
                        post.getUser().getUsername().equals(username)
        ));
    }

    @Test
    void testUpdatePost_Success() {
        // 1. Arrange
        Long postId = 1L;
        String username = "owner";

        // 同一个人 (ID = 1)
        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(user); // 贴主是自己
        existingPost.setTitle("Old Title");

        PostDto updateInfo = new PostDto();
        updateInfo.setTitle("New Title"); // 想改成这个

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);

        // 2. Act
        PostDto result = postService.updatePost(postId, updateInfo, username);

        // 3. Assert
        assertEquals("New Title", result.getTitle()); // 验证返回值变了

        // Verify: 验证 save 确实被调用了
        verify(postRepository).save(existingPost);
    }

    @Test
    void testUpdatePost_PermissionDenied_ThrowException() {
        // 1. Arrange
        Long postId = 1L;
        String hackerName = "hacker";

        // 准备“当前登录用户” (坏人，ID = 2)
        User hacker = new User();
        hacker.setId(2L);
        hacker.setUsername(hackerName);

        // 准备“帖子的原作者” (好人，ID = 1)
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        // 准备帖子 (属于好人)
        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner); // 👈 关键：贴主是 ID 1

        PostDto updateInfo = new PostDto();
        updateInfo.setTitle("Hacked Title");

        // Mock
        when(userRepository.findByUsername(hackerName)).thenReturn(Optional.of(hacker));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // 2. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.updatePost(postId, updateInfo, hackerName);
        });

        assertEquals("You don't have permission to update this post", exception.getMessage());

        // 3. Verify (防守)
        // 确保没有执行 save，帖子没被篡改
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testUpdatePost_PostNotFound_ThrowException() {
        // --- 1. Arrange ---
        Long postId = 999L;
        String username = "user";
        User user = new User();

        PostDto updateDto = new PostDto();

        // 查人能查到
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        // 查贴查不到
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // --- 2. Act & Assert ---
        // 注意：这里要用你的自定义异常类 NotFoundException
        assertThrows(NotFoundException.class, () -> {
            postService.updatePost(postId, updateDto, username);
        });

        // 验证 save 没被调用
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testDeletePost_Success() {
        // --- 1. Arrange ---
        Long postId = 10L;
        String username = "owner";

        // 准备当前用户 (Owner, ID = 1)
        User owner = new User();
        owner.setId(1L);
        owner.setUsername(username);

        // 准备要删的帖子 (归属人 ID = 1)
        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner);

        // Mock: 查人、查贴都成功
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // --- 2. Act ---
        postService.deletePost(postId, username);

        // --- 3. Verify ---
        // 关键：验证 deleteById 方法真的被调用了，而且参数是 10L
        verify(postRepository).deleteById(postId);
    }

    @Test
    void testDeletePost_PermissionDenied_ThrowException() {
        // --- 1. Arrange ---
        Long postId = 10L;
        String hackerName = "hacker";

        // 坏人 (ID = 2)
        User hacker = new User();
        hacker.setId(2L);
        hacker.setUsername(hackerName);

        // 好人 (ID = 1)
        User owner = new User();
        owner.setId(1L);

        // 帖子属于好人
        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner);

        // Mock
        when(userRepository.findByUsername(hackerName)).thenReturn(Optional.of(hacker));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        // --- 2. Act & Assert ---
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.deletePost(postId, hackerName);
        });

        assertEquals("You don't have permission to delete this post", exception.getMessage());

        // --- 3. Verify (重要防守) ---
        // 确保数据库没动
        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeletePost_PostNotFound_ThrowException() {
        // --- 1. Arrange ---
        Long postId = 999L;
        String username = "user";

        // 查人能查到 (为了让代码走到查贴那一步)
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // 查贴查不到
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // --- 2. Act & Assert ---
        // 注意：要用你的 NotFoundException
        assertThrows(NotFoundException.class, () -> {
            postService.deletePost(postId, username);
        });

        // 验证 deleteById 没被调用
        verify(postRepository, never()).deleteById(anyLong());
    }
}
