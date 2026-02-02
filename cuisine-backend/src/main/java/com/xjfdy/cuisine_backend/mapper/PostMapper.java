package com.xjfdy.cuisine_backend.mapper;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.entity.Post;

public class PostMapper {

    public static PostDto mapToPostDto(Post post) {

        post.setTransientFields();

        return new PostDto(
                post.getPostId(),
                post.getTitle(),
                post.getBody(),
                post.getImageUrl(),
                post.getUserId(),
                post.getAuthorName(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public static Post mapToPost(PostDto postDto) {
        return new Post(
                postDto.getPostId(),
                postDto.getTitle(),
                postDto.getBody(),
                postDto.getImageUrl()
        );
    }

}
