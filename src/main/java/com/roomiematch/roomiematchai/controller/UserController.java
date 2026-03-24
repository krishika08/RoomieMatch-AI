package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// created a controller to handle the request from the client
@RestController
// sets the base path url for all the methods in the controller
@RequestMapping("/api/users")
public class UserController {

    // it has private final field of type UserService
    private final UserService userService;

    // constructor injection
    public UserController(UserService userService) {
        // spring will automatically inject the UserService bean here
        this.userService = userService;
    }

    @PostMapping("/register")
    // @RequestBody is used to bind the HTTP request body to the UserRequestDTO object
    public String registerUser(@Valid @RequestBody UserRequestDTO request) {
        // calls the registerUser method of the UserService class
        return userService.registerUser(request);
    }
}
