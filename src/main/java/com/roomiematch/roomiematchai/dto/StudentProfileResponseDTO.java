package com.roomiematch.roomiematchai.dto;

import com.roomiematch.roomiematchai.entity.StudentProfile;
import lombok.Data;

@Data
public class StudentProfileResponseDTO {

    private Long id;
    private Long userId;
    private String userEmail;
    private String sleepSchedule;
    private String cleanlinessLevel;
    private String noiseTolerance;
    private String socialLevel;
    private String studyHabits;
    private String guestFrequency;
    private String roomTemperature;

    public StudentProfileResponseDTO(StudentProfile profile) {
        this.id = profile.getId();
        this.userId = profile.getUser().getId();
        this.userEmail = profile.getUser().getEmail();
        this.sleepSchedule = profile.getSleepSchedule();
        this.cleanlinessLevel = profile.getCleanlinessLevel();
        this.noiseTolerance = profile.getNoiseTolerance();
        this.socialLevel = profile.getSocialLevel();
        this.studyHabits = profile.getStudyHabits();
        this.guestFrequency = profile.getGuestFrequency();
        this.roomTemperature = profile.getRoomTemperature();
    }
}
