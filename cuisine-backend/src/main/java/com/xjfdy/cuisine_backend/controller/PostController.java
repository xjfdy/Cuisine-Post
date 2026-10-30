package com.xjfdy.cuisine_backend.controller;

import com.xjfdy.cuisine_backend.dto.PostDto;
import com.xjfdy.cuisine_backend.dto.PostPageResponse;
import com.xjfdy.cuisine_backend.dto.userDto.MessageResponseDto;
import com.xjfdy.cuisine_backend.exception.UnauthorizedException;
import com.xjfdy.cuisine_backend.security.JwtTokenProvider;
import com.xjfdy.cuisine_backend.service.PostService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;

    public PostController(PostService postService, JwtTokenProvider jwtTokenProvider) {
        this.postService = postService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // GET /api/post?pageNo=0&pageSize=5&query=poutine
    @GetMapping
    public ResponseEntity<PostPageResponse> getPosts(
            @RequestParam(name = "pageNo", defaultValue = "0") int pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "query", defaultValue = "") String query) {
        return ResponseEntity.ok(postService.getPosts(pageNo, pageSize, query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping
    public ResponseEntity<PostDto> createPost(
            @RequestBody PostDto postDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsernameFromToken(authHeader);
        PostDto newPost = postService.createPost(postDto, username);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable("id") Long id,
            @RequestBody PostDto postDto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsernameFromToken(authHeader);
        return ResponseEntity.ok(postService.updatePost(id, postDto, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deletePost(
            @PathVariable("id") Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = getUsernameFromToken(authHeader);
        postService.deletePost(id, username);
        return ResponseEntity.ok(new MessageResponseDto("Post deleted successfully"));
    }

    // Errors are no longer caught here: GlobalExceptionHandler maps them to 400/401/403/404
    private String getUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Please login first");
        }
        try {
            return jwtTokenProvider.getUsername(authHeader.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            // bad signature, malformed or expired token
            throw new UnauthorizedException("Your session has expired, please login again");
        }
    }
}