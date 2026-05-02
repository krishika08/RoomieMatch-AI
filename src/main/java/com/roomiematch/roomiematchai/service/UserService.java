package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.UserResponseDTO;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Service bean containing user-related business logic
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Fetches all users and maps to DTOs (never expose raw entities)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(user.getId(), user.getEmail()))
                .collect(Collectors.toList());
    }
}

