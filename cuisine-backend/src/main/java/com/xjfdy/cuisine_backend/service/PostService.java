package com.xjfdy.cuisine_backend.service;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.dto.PostPageResponse;

public interface PostService {

    PostDto createPost(PostDto postDto, String username);

    PostDto getPostById(Long id);

    PostPageResponse getPosts(int pageNo, int pageSize, String query);

    PostDto updatePost(Long id, PostDto postDto, String username);

    void deletePost(Long id, String username);
}