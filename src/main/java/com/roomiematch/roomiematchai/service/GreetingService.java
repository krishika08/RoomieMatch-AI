package com.roomiematch.roomiematchai.service;

import org.springframework.stereotype.Service;

// Service bean containing greeting-related business logic
@Service
public class GreetingService {

    // Returns a welcome message
    public String greet() {
        return "Welcome to RoomieMatch AI";
    }
}
