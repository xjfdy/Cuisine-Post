package com.xjfdy.cuisine_backend.service.impl;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.dto.PostPageResponse;
import com.xjfdy.cuisine_backend.entity.Post;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.exception.ForbiddenException;
import com.xjfdy.cuisine_backend.exception.NotFoundException;
import com.xjfdy.cuisine_backend.mapper.PostMapper;
import com.xjfdy.cuisine_backend.repository.PostRepository;
import com.xjfdy.cuisine_backend.repository.UserRepository;
import com.xjfdy.cuisine_backend.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PostServiceImpl implements PostService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TEXT_COLUMN_LENGTH = 255;

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PostDto createPost(PostDto postDto, String username) {
        validatePost(postDto);
        User user = findUser(username);

        Post post = PostMapper.mapToPost(postDto);
        post.setUser(user);
        Post savedPost = postRepository.save(post);
        return PostMapper.mapToPostDto(savedPost);
    }

    @Override
    public PostDto getPostById(Long id) {
        return PostMapper.mapToPostDto(findPost(id));
    }

    @Override
    public PostPageResponse getPosts(int pageNo, int pageSize, String query) {
        // not trust page params from the client
        int safePageNo = Math.max(pageNo, 0);
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        // new page first
        Pageable pageable = PageRequest.of(safePageNo, safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAt", "postId"));

        Page<Post> page = (query == null || query.isBlank())
                ? postRepository.findAll(pageable)
                : postRepository.findByTitleContainingIgnoreCase(query.trim(), pageable);

        List<PostDto> content = page.getContent().stream()
                .map(PostMapper::mapToPostDto)
                .toList();

        return new PostPageResponse(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    @Override
    public PostDto updatePost(Long id, PostDto updatedPostDto, String username) {
        validatePost(updatedPostDto);

        User currentUser = findUser(username);
        Post post = findPost(id);

        // only the author can edit
        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new ForbiddenException("You don't have permission to update this post");
        }

        post.setTitle(updatedPostDto.getTitle());
        post.setBody(updatedPostDto.getBody());
        post.setImageUrl(updatedPostDto.getImageUrl());

        Post savedPost = postRepository.save(post);
        return PostMapper.mapToPostDto(savedPost);
    }

    @Override
    public void deletePost(Long id, String username) {
        User currentUser = findUser(username);
        Post post = findPost(id);

        // only the author can delete
        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new ForbiddenException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    private Post findPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
    }

    // Server-side validation: the frontend checks can be bypassed (e.g. with Postman)
    private void validatePost(PostDto postDto) {
        if (postDto.getTitle() == null || postDto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (postDto.getTitle().length() > MAX_TEXT_COLUMN_LENGTH) {
            throw new IllegalArgumentException("Title must be at most 255 characters");
        }
        if (postDto.getBody() == null || postDto.getBody().isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty");
        }
        if (postDto.getImageUrl() != null && postDto.getImageUrl().length() > MAX_TEXT_COLUMN_LENGTH) {
            throw new IllegalArgumentException("Image URL must be at most 255 characters");
        }
    }
}