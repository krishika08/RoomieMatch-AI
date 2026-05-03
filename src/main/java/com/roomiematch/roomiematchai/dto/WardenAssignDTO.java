package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for assigning a warden to a hostel.
 * Used by MANAGER via POST /manager/assign-warden.
 */
@Data
public class WardenAssignDTO {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Hostel is required")
    private String hostel;

    private String name;
}
