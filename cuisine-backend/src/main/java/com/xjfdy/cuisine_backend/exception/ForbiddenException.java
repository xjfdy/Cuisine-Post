package com.xjfdy.cuisine_backend.exception;

// Logged in but not allowed to touch this resource -> 403
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}