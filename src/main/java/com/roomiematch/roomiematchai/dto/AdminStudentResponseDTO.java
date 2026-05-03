package com.roomiematch.roomiematchai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin's student list view.
 * Includes user info + org/hostel + whether profile is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStudentResponseDTO {
    private Long id;
    private String email;
    private String role;
    private String organization;
    private String hostel;
    private boolean profileComplete;
    private String createdAt;
}
