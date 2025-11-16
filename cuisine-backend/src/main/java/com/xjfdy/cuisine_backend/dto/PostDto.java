package com.xjfdy.cuisine_backend.dto;

import java.time.LocalDateTime;

public class PostDto {

    private long postId;
    private String title;
    private String body;
    private String imageUrl;
    private Long userId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostDto() {
    }

    public PostDto(long postId, String title, String body, String imageUrl) {
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
    }

    public PostDto(Long postId, String title, String body, String imageUrl,
                   Long userId, String authorName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.postId = postId;
        this.title = title;
        this.body = body;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.authorName = authorName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PostDto{" +
                "postId=" + postId +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", userId=" + userId +
                ", authorName='" + authorName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
