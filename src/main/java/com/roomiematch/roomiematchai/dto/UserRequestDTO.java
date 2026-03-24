package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.*;

public class UserRequestDTO {
    @Email
    public String email;

    @NotBlank
    @Size(min = 4)
    public String password;
}
