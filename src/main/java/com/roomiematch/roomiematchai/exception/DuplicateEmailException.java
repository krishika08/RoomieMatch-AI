package com.roomiematch.roomiematchai.exception;

// Custom exception for duplicate email registration attempts
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email already registered: " + email);
    }
}
