package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import org.springframework.stereotype.Service;

// Service bean containing user-related business logic
@Service
public class UserService {

    // Registers a user and returns a confirmation message
    public String registerUser(UserRequestDTO request) {
        return "User registered with email: " + request.getEmail();
    }
}
