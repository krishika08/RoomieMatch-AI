package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentProfileRequestDTO {

    @NotBlank(message = "Sleep schedule cannot be blank")
    private String sleepSchedule;

    @NotBlank(message = "Cleanliness level cannot be blank")
    private String cleanlinessLevel;

    @NotBlank(message = "Noise tolerance cannot be blank")
    private String noiseTolerance;

    @NotBlank(message = "Social level cannot be blank")
    private String socialLevel;

    @NotBlank(message = "Study habits cannot be blank")
    private String studyHabits;

    @NotBlank(message = "Guest frequency cannot be blank")
    private String guestFrequency;

    @NotBlank(message = "Room temperature preference cannot be blank")
    private String roomTemperature;
}
