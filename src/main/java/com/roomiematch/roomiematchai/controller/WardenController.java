package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.*;
import com.roomiematch.roomiematchai.service.WardenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Warden API endpoints — hostel-level admin.
 * Protected: WARDEN and MANAGER roles can access.
 */
@RestController
@RequestMapping("/warden")
@PreAuthorize("hasAnyRole('WARDEN', 'MANAGER')")
public class WardenController {

    private final WardenService wardenService;

    public WardenController(WardenService wardenService) {
        this.wardenService = wardenService;
    }

    /**
     * GET /warden/students
     * Returns students in the warden's assigned hostel.
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<AdminStudentResponseDTO>>> getStudents() {
        List<AdminStudentResponseDTO> students = wardenService.getStudents();
        return ResponseEntity.ok(new ApiResponse<>("Students fetched successfully", students));
    }

    /**
     * POST /warden/create
     * Creates a sub-warden account with the same hostel assignment.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminStudentResponseDTO>> createWarden(
            @Valid @RequestBody WardenCreateDTO dto) {
        AdminStudentResponseDTO result = wardenService.createWarden(dto);
        return ResponseEntity.ok(new ApiResponse<>("Warden created successfully", result));
    }

    /**
     * GET /warden/requests?status=PENDING
     * Returns requests involving users in the warden's hostel.
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) String status) {
        List<RoommateRequestResponseDTO> requests = wardenService.getRequests(status);
        return ResponseEntity.ok(new ApiResponse<>("Requests fetched successfully", requests));
    }

    /**
     * PUT /warden/requests/{id}/respond?status=ACCEPTED
     * Accept or reject a pending request within the warden's hostel.
     */
    @PutMapping("/requests/{id}/respond")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> respondToRequest(
            @PathVariable Long id,
            @RequestParam String status) {
        RoommateRequestResponseDTO result = wardenService.respondToRequest(id, status);
        return ResponseEntity.ok(new ApiResponse<>("Request " + status.toLowerCase() + " successfully", result));
    }

    /**
     * GET /warden/assignments
     * Returns room assignments in the warden's hostel.
     */
    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<RoomAssignmentResponseDTO>>> getAssignments() {
        List<RoomAssignmentResponseDTO> assignments = wardenService.getAssignments();
        return ResponseEntity.ok(new ApiResponse<>("Assignments fetched successfully", assignments));
    }
}

