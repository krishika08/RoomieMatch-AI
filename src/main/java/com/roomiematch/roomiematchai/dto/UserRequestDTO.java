package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.*;

// made a DTO ( client -> controller -> service )
public class UserRequestDTO {

    @Email
    public String email;

    @NotBlank
    @Size(min = 4)
    public String password;
}
