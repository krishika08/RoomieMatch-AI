package com.roomiematch.roomiematchai.controller;

// spring tools to import 
import org.springframework.web.bind.annotation.GetMapping; // get request --> maps HTTPS 
import org.springframework.web.bind.annotation.RequestMapping; // request mapping(/LOGIN )
import org.springframework.web.bind.annotation.RestController; // rest api contoller

// TELLS SPRING THIS IS A REST CONTROLLER
@RestController
// BASE URL FOR ALL ENDPOINTS IN THIS CONTROLLER
@RequestMapping("/api")
public class HealthController {
    // GET REQUEST --> maps HTTPS GET requests to this method
    @GetMapping("/health")
    // this method will be executed when a GET request is made to /api/health
    public String healthCheck() {
        return "RoomieMatch AI Backend Running";
    }
}
