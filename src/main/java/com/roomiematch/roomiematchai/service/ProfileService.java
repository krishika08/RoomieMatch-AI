package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.StudentProfileRequestDTO;
import com.roomiematch.roomiematchai.dto.StudentProfileResponseDTO;
import com.roomiematch.roomiematchai.entity.StudentProfile;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final AuthContextService authContext;

    public ProfileService(StudentProfileRepository profileRepository, AuthContextService authContext) {
        this.profileRepository = profileRepository;
        this.authContext = authContext;
    }

    @Transactional
    public StudentProfileResponseDTO createProfile(StudentProfileRequestDTO request) {
        User user = authContext.getLoggedInUser();

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
        User user = authContext.getLoggedInUser();
        StudentProfile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user"));
        return new StudentProfileResponseDTO(profile);
    }

    @Transactional
    public StudentProfileResponseDTO updateProfile(StudentProfileRequestDTO request) {
        User user = authContext.getLoggedInUser();
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

