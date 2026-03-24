package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.dto.UserResponseDTO;
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

    // Registers a user after checking for duplicate email; returns confirmation message
    public String registerUser(UserRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        userRepository.save(user);

        return "User registered successfully";
    }

    // Fetches all users and maps them to response DTOs (excludes password)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getEmail()))
                .toList();
    }
}
