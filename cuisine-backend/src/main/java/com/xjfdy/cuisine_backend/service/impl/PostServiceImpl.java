package com.xjfdy.cuisine_backend.service.impl;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.entity.Post;
import com.xjfdy.cuisine_backend.entity.User;
import com.xjfdy.cuisine_backend.exception.NotFoundException;
import com.xjfdy.cuisine_backend.mapper.PostMapper;
import com.xjfdy.cuisine_backend.repository.PostRepository;
import com.xjfdy.cuisine_backend.repository.UserRepository;
import com.xjfdy.cuisine_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public PostDto createPost(PostDto postDto, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found:" + username));

        Post post = PostMapper.mapToPost(postDto);
        post.setUser(user);
        Post savedPost = postRepository.save(post);

        savedPost.setTransientFields();
        return PostMapper.mapToPostDto(savedPost);
    }

    @Override
    public PostDto getPostById(Long id) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Provided ID not found" + id));

        post.setTransientFields();
        return PostMapper.mapToPostDto(post);
    }

    @Override
    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream()
                .map(post -> {
                    post.setTransientFields();
                    return PostMapper.mapToPostDto(post);
                })
                .collect(Collectors.toList());
    }

    @Override
    public PostDto updatePost(Long id, PostDto updatedPostDto, String username) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found:" + username));

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Provided ID not found" + id));

        //check the availability
        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("You don't have permission to update this post");
        }

        //update the content of the post
        post.setBody(updatedPostDto.getBody());
        post.setTitle(updatedPostDto.getTitle());
        post.setImageUrl(updatedPostDto.getImageUrl());

        //save the post
        Post savedPost = postRepository.save(post);

        savedPost.setTransientFields();
        return PostMapper.mapToPostDto(savedPost);
    }

    @Override
    public void deletePost(Long id, String username) {

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found:" + username));

        Post post = postRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Provided ID not found" + id)
        );

        if (!Objects.equals(post.getUser().getId(), currentUser.getId())) {
            throw new RuntimeException("You don't have permission to delete this post");
        }

        postRepository.deleteById(id);
    }
}
