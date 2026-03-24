package com.roomiematch.roomiematchai.exception;

// Custom exception for failed login attempts
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
