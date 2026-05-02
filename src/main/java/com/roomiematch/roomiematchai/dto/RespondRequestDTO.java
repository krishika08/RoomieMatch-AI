package com.roomiematch.roomiematchai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondRequestDTO {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotBlank(message = "Status is required (ACCEPTED or REJECTED)")
    private String status;
}
