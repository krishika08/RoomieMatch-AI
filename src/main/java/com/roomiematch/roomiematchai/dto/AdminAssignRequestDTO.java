package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAssignRequestDTO {

    @NotNull(message = "userId1 is required")
    private Long userId1;

    @NotNull(message = "userId2 is required")
    private Long userId2;
}
