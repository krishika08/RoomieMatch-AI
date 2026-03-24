package com.roomiematch.roomiematchai.dto;

// DTO for user response (service -> controller -> client)
// Excludes sensitive fields like password
public class UserResponseDTO {

    private Long id;
    private String email;

    public UserResponseDTO() {
    }

    public UserResponseDTO(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
