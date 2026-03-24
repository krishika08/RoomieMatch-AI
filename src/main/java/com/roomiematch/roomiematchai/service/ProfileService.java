package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.StudentProfileRequestDTO;
import com.roomiematch.roomiematchai.dto.StudentProfileResponseDTO;
import com.roomiematch.roomiematchai.entity.StudentProfile;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(StudentProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Logged in user not found"));
    }

    public StudentProfileResponseDTO createProfile(StudentProfileRequestDTO request) {
        User user = getLoggedInUser();

        if (profileRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalStateException("Profile already exists for this user.");
        }

        StudentProfile profile = new StudentProfile();
        profile.setUser(user);
        profile.setSleepSchedule(request.getSleepSchedule());
        profile.setCleanlinessLevel(request.getCleanlinessLevel());
        profile.setNoiseTolerance(request.getNoiseTolerance());
        profile.setSocialLevel(request.getSocialLevel());
        profile.setStudyHabits(request.getStudyHabits());
        profile.setGuestFrequency(request.getGuestFrequency());
        profile.setRoomTemperature(request.getRoomTemperature());

        StudentProfile savedProfile = profileRepository.save(profile);
        return new StudentProfileResponseDTO(savedProfile);
    }

    public StudentProfileResponseDTO getProfile() {
        User user = getLoggedInUser();
        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user"));
        return new StudentProfileResponseDTO(profile);
    }

    public StudentProfileResponseDTO updateProfile(StudentProfileRequestDTO request) {
        User user = getLoggedInUser();
        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user"));

        profile.setSleepSchedule(request.getSleepSchedule());
        profile.setCleanlinessLevel(request.getCleanlinessLevel());
        profile.setNoiseTolerance(request.getNoiseTolerance());
        profile.setSocialLevel(request.getSocialLevel());
        profile.setStudyHabits(request.getStudyHabits());
        profile.setGuestFrequency(request.getGuestFrequency());
        profile.setRoomTemperature(request.getRoomTemperature());

        StudentProfile updatedProfile = profileRepository.save(profile);
        return new StudentProfileResponseDTO(updatedProfile);
    }
}
