package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.PostDto;

import java.util.List;

public interface PostService {

    PostDto createPost(PostDto postDto, String username);

    PostDto getPostById(Long id);

    List<PostDto> getAllPosts();

    PostDto updatePost(Long id, PostDto postDto, String username);

    void deletePost(Long id, String username);
}
