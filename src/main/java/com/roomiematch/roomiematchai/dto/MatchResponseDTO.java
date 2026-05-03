package com.roomiematch.roomiematchai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDTO {

    private Long userId;
    private String email;
    private int compatibilityScore;

    /** Per-trait score breakdown (sleep, cleanliness, noise, etc.) */
    private ScoreBreakdownDTO breakdown;

    // Profile summary fields
    private String sleepSchedule;
    private String cleanlinessLevel;
    private String noiseTolerance;
    private String socialLevel;
    private String studyHabits;
    private String guestFrequency;
    private String roomTemperature;
}

