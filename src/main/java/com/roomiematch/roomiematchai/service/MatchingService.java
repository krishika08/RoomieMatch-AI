package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.MatchResponseDTO;
import com.roomiematch.roomiematchai.dto.ScoreBreakdownDTO;
import com.roomiematch.roomiematchai.entity.StudentProfile;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoomAssignmentRepository;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final StudentProfileRepository profileRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final AuthContextService authContext;

    public MatchingService(StudentProfileRepository profileRepository,
                           RoomAssignmentRepository assignmentRepository,
                           AuthContextService authContext) {
        this.profileRepository = profileRepository;
        this.assignmentRepository = assignmentRepository;
        this.authContext = authContext;
    }

    /**
     * Gets the logged-in user from SecurityContext, retrieves their profile,
     * compares it against all other profiles using weighted scoring,
     * and returns matches sorted by compatibility (highest first).
     *
     * Users who are already assigned a roommate are excluded from results.
     */
    public List<MatchResponseDTO> getMatchesForCurrentUser() {
        // 1. Get the logged-in user
        User currentUser = authContext.getLoggedInUser();

        // 2. If current user is already assigned, return empty
        if (assignmentRepository.isUserAssigned(currentUser.getId())) {
            return List.of(); // already paired — no more matching needed
        }

        // 3. Get the current user's profile
        StudentProfile currentProfile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Please create your profile first before viewing matches"));

        // 4. Fetch all other profiles (exclude current user, filter by same hostel)
        List<StudentProfile> allProfiles = profileRepository.findAll();
        List<StudentProfile> otherProfiles = allProfiles.stream()
                .filter(profile -> !profile.getUser().getId().equals(currentUser.getId()))
                .filter(profile -> {
                    // Only match with users in the same organization
                    String myOrg = currentUser.getOrganization();
                    String theirOrg = profile.getUser().getOrganization();
                    if (myOrg == null || theirOrg == null) return false;
                    return myOrg.equalsIgnoreCase(theirOrg);
                })
                .filter(profile -> {
                    // Only match with users in the same hostel
                    String myHostel = currentUser.getHostel();
                    String theirHostel = profile.getUser().getHostel();
                    if (myHostel == null || theirHostel == null) return false;
                    return myHostel.equalsIgnoreCase(theirHostel);
                })
                .filter(profile -> {
                    // Exclude users who are already assigned a roommate
                    return !assignmentRepository.isUserAssigned(profile.getUser().getId());
                })
                .collect(Collectors.toList());

        // 5. Score each profile with per-trait breakdown and build response
        return otherProfiles.stream()
                .map(otherProfile -> {
                    ScoreBreakdownDTO breakdown = calculateBreakdown(currentProfile, otherProfile);
                    int totalScore = breakdown.getSleepSchedule()
                                   + breakdown.getCleanliness()
                                   + breakdown.getNoiseTolerance()
                                   + breakdown.getSocialLevel()
                                   + breakdown.getStudyHabits()
                                   + breakdown.getGuestFrequency()
                                   + breakdown.getRoomTemperature();

                    MatchResponseDTO dto = new MatchResponseDTO();
                    dto.setUserId(otherProfile.getUser().getId());
                    dto.setEmail(otherProfile.getUser().getEmail());
                    dto.setCompatibilityScore(totalScore);
                    dto.setBreakdown(breakdown);
                    dto.setSleepSchedule(otherProfile.getSleepSchedule());
                    dto.setCleanlinessLevel(otherProfile.getCleanlinessLevel());
                    dto.setNoiseTolerance(otherProfile.getNoiseTolerance());
                    dto.setSocialLevel(otherProfile.getSocialLevel());
                    dto.setStudyHabits(otherProfile.getStudyHabits());
                    dto.setGuestFrequency(otherProfile.getGuestFrequency());
                    dto.setRoomTemperature(otherProfile.getRoomTemperature());
                    return dto;
                })
                .sorted(Comparator.comparingInt(MatchResponseDTO::getCompatibilityScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns a per-trait score breakdown for two profiles.
     *
     * Weights (total = 100):
     *   sleepSchedule     → 20
     *   cleanlinessLevel  → 20
     *   noiseTolerance    → 15
     *   socialLevel       → 15
     *   studyHabits       → 15
     *   guestFrequency    → 10
     *   roomTemperature   → 5
     */
    private ScoreBreakdownDTO calculateBreakdown(StudentProfile current, StudentProfile other) {
        return new ScoreBreakdownDTO(
            equalsIgnoreCase(current.getSleepSchedule(), other.getSleepSchedule()) ? 20 : 0,
            equalsIgnoreCase(current.getCleanlinessLevel(), other.getCleanlinessLevel()) ? 20 : 0,
            equalsIgnoreCase(current.getNoiseTolerance(), other.getNoiseTolerance()) ? 15 : 0,
            equalsIgnoreCase(current.getSocialLevel(), other.getSocialLevel()) ? 15 : 0,
            equalsIgnoreCase(current.getStudyHabits(), other.getStudyHabits()) ? 15 : 0,
            equalsIgnoreCase(current.getGuestFrequency(), other.getGuestFrequency()) ? 10 : 0,
            equalsIgnoreCase(current.getRoomTemperature(), other.getRoomTemperature()) ? 5 : 0
        );
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
