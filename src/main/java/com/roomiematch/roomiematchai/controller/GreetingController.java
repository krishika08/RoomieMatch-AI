package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.service.GreetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Handles greeting-related API requests
@RestController
@RequestMapping("/api")
public class GreetingController {

    private final GreetingService greetingService;

    // Constructor injection
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    // Returns a welcome message from the greeting service
    @GetMapping("/greet")
    public ResponseEntity<ApiResponse<String>> getGreeting() {
        return ResponseEntity.ok(new ApiResponse<>("Greeting fetched", greetingService.greet()));
    }
}
