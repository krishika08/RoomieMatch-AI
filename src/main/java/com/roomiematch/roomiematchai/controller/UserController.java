package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.UserResponseDTO;
import com.roomiematch.roomiematchai.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Handles user-related API requests
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor injection — Spring auto-injects UserService bean
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Retrieves all registered users (returns DTOs, not raw entities)
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>("Users fetched successfully", users));
    }
}

