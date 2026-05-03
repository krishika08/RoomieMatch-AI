package com.roomiematch.roomiematchai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-trait score breakdown for a match.
 * Each field shows how many points were earned for that trait
 * based on the weighted scoring system.
 *
 * Weights: sleep=20, cleanliness=20, noise=15, social=15,
 *          study=15, guests=10, temperature=5  (total=100)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreBreakdownDTO {
    private int sleepSchedule;      // max 20
    private int cleanliness;        // max 20
    private int noiseTolerance;     // max 15
    private int socialLevel;        // max 15
    private int studyHabits;        // max 15
    private int guestFrequency;     // max 10
    private int roomTemperature;    // max 5
}
