package com.roomiematch.roomiematchai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String sleepSchedule;

    @Column(nullable = false)
    private String cleanlinessLevel;

    @Column(nullable = false)
    private String noiseTolerance;

    @Column(nullable = false)
    private String socialLevel;

    @Column(nullable = false)
    private String studyHabits;

    @Column(nullable = false)
    private String guestFrequency;

    @Column(nullable = false)
    private String roomTemperature;
}
