package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.MatchResponseDTO;
import com.roomiematch.roomiematchai.entity.StudentProfile;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final StudentProfileRepository profileRepository;
    private final AuthContextService authContext;

    public MatchingService(StudentProfileRepository profileRepository, AuthContextService authContext) {
        this.profileRepository = profileRepository;
        this.authContext = authContext;
    }

    /**
     * Gets the logged-in user from SecurityContext, retrieves their profile,
     * compares it against all other profiles using weighted scoring,
     * and returns matches sorted by compatibility (highest first).
     */
    public List<MatchResponseDTO> getMatchesForCurrentUser() {
        // 1. Get the logged-in user
        User currentUser = authContext.getLoggedInUser();

        // 2. Get the current user's profile
        StudentProfile currentProfile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Please create your profile first before viewing matches"));

        // 3. Fetch all other profiles (exclude current user)
        List<StudentProfile> allProfiles = profileRepository.findAll();
        List<StudentProfile> otherProfiles = allProfiles.stream()
                .filter(profile -> !profile.getUser().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        // 4. Score each profile and build response
        return otherProfiles.stream()
                .map(otherProfile -> {
                    int score = calculateCompatibilityScore(currentProfile, otherProfile);
                    return new MatchResponseDTO(
                            otherProfile.getUser().getId(),
                            otherProfile.getUser().getEmail(),
                            score,
                            otherProfile.getSleepSchedule(),
                            otherProfile.getCleanlinessLevel(),
                            otherProfile.getNoiseTolerance(),
                            otherProfile.getSocialLevel(),
                            otherProfile.getStudyHabits(),
                            otherProfile.getGuestFrequency(),
                            otherProfile.getRoomTemperature()
                    );
                })
                .sorted(Comparator.comparingInt(MatchResponseDTO::getCompatibilityScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Compares two profiles field-by-field using weighted scoring.
     *
     * Weights (total = 100):
     *   sleepSchedule     → 20
     *   cleanlinessLevel  → 20
     *   noiseTolerance    → 15
     *   socialLevel       → 15
     *   studyHabits       → 15
     *   guestFrequency    → 10
     *   roomTemperature   → 5
     *
     * If values match (case-insensitive) → add the weight to the score.
     */
    private int calculateCompatibilityScore(StudentProfile current, StudentProfile other) {
        int score = 0;

        if (equalsIgnoreCase(current.getSleepSchedule(), other.getSleepSchedule())) {
            score += 20;
        }
        if (equalsIgnoreCase(current.getCleanlinessLevel(), other.getCleanlinessLevel())) {
            score += 20;
        }
        if (equalsIgnoreCase(current.getNoiseTolerance(), other.getNoiseTolerance())) {
            score += 15;
        }
        if (equalsIgnoreCase(current.getSocialLevel(), other.getSocialLevel())) {
            score += 15;
        }
        if (equalsIgnoreCase(current.getStudyHabits(), other.getStudyHabits())) {
            score += 15;
        }
        if (equalsIgnoreCase(current.getGuestFrequency(), other.getGuestFrequency())) {
            score += 10;
        }
        if (equalsIgnoreCase(current.getRoomTemperature(), other.getRoomTemperature())) {
            score += 5;
        }

        return score;
    }

    /**
     * Null-safe, case-insensitive string comparison.
     */
    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
