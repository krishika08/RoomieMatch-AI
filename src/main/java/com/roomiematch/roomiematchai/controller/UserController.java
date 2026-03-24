package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody UserRequestDTO request) {
        return userService.registerUser(request);
    }
}
