package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Shared service to extract the currently authenticated user from the JWT/SecurityContext.
 * Eliminates duplication of getLoggedInUser() across ProfileService, MatchingService,
 * and RoommateRequestService.
 */
@Service
public class AuthContextService {

    private final UserRepository userRepository;

    public AuthContextService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the User entity for the currently authenticated request.
     * @throws ResourceNotFoundException if the JWT email doesn't match any user in the database
     */
    public User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
    }
}
