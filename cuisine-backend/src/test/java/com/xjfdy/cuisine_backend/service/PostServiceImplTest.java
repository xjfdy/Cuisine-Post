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

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setUserId(100L);
            return p;
        });

        PostDto result = postService.createPost(inputDto, username);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());

        verify(postRepository).save(argThat(post ->
                post.getUser().getUsername().equals(username)
        ));
    }

    @Test
    void testCreatePost_UserNotFound_ThrowException() {
        String nonExistentUser = "ghost";
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Ghost Post");

        when(userRepository.findByUsername(nonExistentUser))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            postService.createPost(inputDto, nonExistentUser);
        });

        // 关键防守：因为人没找到，绝对不能去调用保存方法！
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testCreatePost_WithImage_Success() {
        String username = "photographer";
        String validUrl = "http://example.com/image.jpg";

        PostDto inputDto = new PostDto();
        inputDto.setTitle("Photo Post");
        inputDto.setBody("Look at this!");
        inputDto.setImageUrl(validUrl);

        User mockUser = new User();
        mockUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post p = invocation.getArgument(0);
            p.setUserId(200L);
            return p;
        });

        PostDto result = postService.createPost(inputDto, username);

        assertNotNull(result);
        assertEquals(validUrl, result.getImageUrl());

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

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(user);
        existingPost.setTitle("Old Title");

        PostDto updateInfo = new PostDto();
        updateInfo.setTitle("New Title");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(existingPost);

        PostDto result = postService.updatePost(postId, updateInfo, username);

        assertEquals("New Title", result.getTitle());

        verify(postRepository).save(existingPost);
    }

    @Test
    void testUpdatePost_PermissionDenied_ThrowException() {
        Long postId = 1L;
        String hackerName = "hacker";

        User hacker = new User();
        hacker.setId(2L);
        hacker.setUsername(hackerName);

        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner);

        PostDto updateInfo = new PostDto();
        updateInfo.setTitle("Hacked Title");

        when(userRepository.findByUsername(hackerName)).thenReturn(Optional.of(hacker));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.updatePost(postId, updateInfo, hackerName);
        });

        assertEquals("You don't have permission to update this post", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testUpdatePost_PostNotFound_ThrowException() {
        Long postId = 999L;
        String username = "user";
        User user = new User();

        PostDto updateDto = new PostDto();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            postService.updatePost(postId, updateDto, username);
        });

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testDeletePost_Success() {
        Long postId = 10L;
        String username = "owner";

        User owner = new User();
        owner.setId(1L);
        owner.setUsername(username);

        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(owner));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        postService.deletePost(postId, username);

        verify(postRepository).deleteById(postId);
    }

    @Test
    void testDeletePost_PermissionDenied_ThrowException() {
        Long postId = 10L;
        String hackerName = "hacker";

        User hacker = new User();
        hacker.setId(2L);
        hacker.setUsername(hackerName);

        User owner = new User();
        owner.setId(1L);

        Post existingPost = new Post();
        existingPost.setUserId(postId);
        existingPost.setUser(owner);

        when(userRepository.findByUsername(hackerName)).thenReturn(Optional.of(hacker));
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.deletePost(postId, hackerName);
        });

        assertEquals("You don't have permission to delete this post", exception.getMessage());

        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeletePost_PostNotFound_ThrowException() {
        Long postId = 999L;
        String username = "user";

        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            postService.deletePost(postId, username);
        });

        verify(postRepository, never()).deleteById(anyLong());
    }
}
