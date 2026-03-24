package com.roomiematch.roomiematchai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Handles health-check API requests
@RestController
@RequestMapping("/api")
public class HealthController {

    // Returns a simple status message to confirm the backend is running
    @GetMapping("/health")
    public String healthCheck() {
        return "RoomieMatch AI Backend Running";
    }
}
