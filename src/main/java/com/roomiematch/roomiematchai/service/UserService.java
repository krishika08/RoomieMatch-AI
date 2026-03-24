package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
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

    // Registers a user and returns a confirmation message
    public String registerUser(UserRequestDTO request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        
        userRepository.save(user);
        
        return "User saved in DB";
    }

    // Fetches all users from the database
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
