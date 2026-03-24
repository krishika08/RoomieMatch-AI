package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Handles health-check API requests
@RestController
@RequestMapping("/api")
public class HealthController {

    // Returns a simple status message to confirm the backend is running
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(new ApiResponse<>("Server is running", "RoomieMatch AI Backend Running"));
    }
}
