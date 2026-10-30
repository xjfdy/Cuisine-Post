package com.xjfdy.cuisine_backend.exception;

// Missing, invalid or expired token -> 401
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}