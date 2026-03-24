package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String registerUser(UserRequestDTO request) {
        return "User registered with email: " + request.email;
    }
}
