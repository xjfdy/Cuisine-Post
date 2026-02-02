package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.dto.PostResponse;


public interface PostService {

    PostDto createPost(PostDto postDto, String username);

    PostDto getPostById(Long id);

    PostResponse getAllPosts(int pageNo, int pageSize, String sortBy, String sortDir, String search);

    PostDto updatePost(Long id, PostDto postDto, String username);

    void deletePost(Long id, String username);
}
