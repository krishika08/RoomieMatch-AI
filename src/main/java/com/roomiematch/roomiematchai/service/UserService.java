package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Service bean containing user-related business logic
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Fetches all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
