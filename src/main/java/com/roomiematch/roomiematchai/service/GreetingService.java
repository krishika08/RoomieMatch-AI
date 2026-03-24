package com.roomiematch.roomiematchai.service;

// service class --> contains business logic
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    // method that returns a greeting message
    public String greet() {
        return "Welcome to RoomieMatch AI";
    }
}
