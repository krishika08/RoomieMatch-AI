package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.LoginRequestDTO;
import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> signup(@Valid @RequestBody UserRequestDTO request) {
        User savedUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("User successfully registered", savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequestDTO request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>("Login successful", token));
    }
}
