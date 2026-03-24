package com.roomiematch.roomiematchai.controller;

// import GRRETING service class CTREATED EARLIER 
import com.roomiematch.roomiematchai.service.GreetingService;
// IMPORTING FOR WEB REQUEST MAPPING 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TELLS SPRING THIS IS A REST CONTROLLER
@RestController
// BASE URL FOR ALL ENDPOINTS IN THIS CONTROLLER
@RequestMapping("/api")
public class GreetingController {

    // DEPENDENCY INJECTION --> SPRING WILL AUTOMATICALLY CREATE AND PROVIDE AN INSTANCE OF GREETING SERVICE
    private final GreetingService greetingService;

    // Constructor Injection
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    // this method will be executed when a GET request is made to /api/greet
    @GetMapping("/greet")
    public String getGreeting() {
        // CALLING THE GREET METHOD FROM THE GREETING SERVICE CLASS
        return greetingService.greet();
    }
}
