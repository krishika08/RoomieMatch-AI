package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.RespondRequestDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.service.RoommateRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class RoommateRequestController {

    private final RoommateRequestService requestService;

    public RoommateRequestController(RoommateRequestService requestService) {
        this.requestService = requestService;
    }

    // ──────────────────────────────────────────────
    //  POST /requests/send — Send a roommate request
    //  Sender is extracted from JWT (not passed manually)
    // ──────────────────────────────────────────────
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> sendRequest(
            @Valid @RequestBody RoommateRequestDTO request) {
        RoommateRequestResponseDTO response = requestService.sendRequest(request.getReceiverId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Roommate request sent successfully", response));
    }

    // ──────────────────────────────────────────────
    //  GET /requests/incoming — View requests sent to me
    // ──────────────────────────────────────────────
    @GetMapping("/incoming")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getIncomingRequests() {
        List<RoommateRequestResponseDTO> requests = requestService.getIncomingRequests();
        String message = requests.isEmpty()
                ? "No incoming roommate requests"
                : "Incoming requests retrieved successfully";
        return ResponseEntity.ok(new ApiResponse<>(message, requests));
    }

    // ──────────────────────────────────────────────
    //  GET /requests/sent — View requests I have sent
    // ──────────────────────────────────────────────
    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getSentRequests() {
        List<RoommateRequestResponseDTO> requests = requestService.getSentRequests();
        String message = requests.isEmpty()
                ? "No sent roommate requests"
                : "Sent requests retrieved successfully";
        return ResponseEntity.ok(new ApiResponse<>(message, requests));
    }

    // ──────────────────────────────────────────────
    //  PUT /requests/respond — Accept or Reject a request
    //  Body: { "requestId": 1, "status": "ACCEPTED" }
    // ──────────────────────────────────────────────
    @PutMapping("/respond")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> respondToRequest(
            @Valid @RequestBody RespondRequestDTO request) {
        RoommateRequestResponseDTO response = requestService.respondToRequest(request.getRequestId(), request.getStatus());
        return ResponseEntity.ok(new ApiResponse<>("Request " + request.getStatus().toLowerCase() + " successfully", response));
    }

}
