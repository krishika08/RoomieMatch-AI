package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.LoginRequestDTO;
import com.roomiematch.roomiematchai.dto.UserRequestDTO;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.DuplicateEmailException;
import com.roomiematch.roomiematchai.exception.InvalidCredentialsException;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRequestDTO request) {
        log.info("AuthService: Registering new user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
             log.warn("AuthService: Duplicate registration for email: {}", request.getEmail());
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // Hash the password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public User login(LoginRequestDTO request) {
        log.info("AuthService: Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Match the raw password against the stored BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("AuthService: Successful login for email: {}", request.getEmail());
        return user;
    }
}
