package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO for user registration request (client -> controller -> service)
public class UserRequestDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
