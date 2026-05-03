package com.roomiematch.roomiematchai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for bulk student upload results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {
    private int totalRecords;
    private int created;
    private int skipped;   // duplicates
    private int failed;
    private java.util.List<String> errors;
}
