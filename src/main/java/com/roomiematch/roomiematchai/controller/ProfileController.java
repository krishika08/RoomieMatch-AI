package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.StudentProfileRequestDTO;
import com.roomiematch.roomiematchai.dto.StudentProfileResponseDTO;
import com.roomiematch.roomiematchai.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentProfileResponseDTO>> createProfile(@Valid @RequestBody StudentProfileRequestDTO request) {
        StudentProfileResponseDTO createdProfile = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Profile created successfully", createdProfile));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<StudentProfileResponseDTO>> getProfile() {
        StudentProfileResponseDTO profile = profileService.getProfile();
        return ResponseEntity.ok(new ApiResponse<>("Profile retrieved successfully", profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<StudentProfileResponseDTO>> updateProfile(@Valid @RequestBody StudentProfileRequestDTO request) {
        StudentProfileResponseDTO updatedProfile = profileService.updateProfile(request);
        return ResponseEntity.ok(new ApiResponse<>("Profile updated successfully", updatedProfile));
    }
}
