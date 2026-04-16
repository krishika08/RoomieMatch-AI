package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.MatchResponseDTO;
import com.roomiematch.roomiematchai.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponseDTO>>> getMatches() {
        List<MatchResponseDTO> matches = matchingService.getMatchesForCurrentUser();
        String message = matches.isEmpty()
                ? "No matches found yet. More users need to create their profiles!"
                : "Matches retrieved successfully";
        return ResponseEntity.ok(new ApiResponse<>(message, matches));
    }
}
