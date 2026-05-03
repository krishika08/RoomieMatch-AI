package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.*;
import com.roomiematch.roomiematchai.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Legacy Admin API endpoints — kept for backward compatibility.
 * Protected by Spring Security — only MANAGER and WARDEN roles can access.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('MANAGER', 'WARDEN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<AdminStudentResponseDTO>>> getStudents(
            @RequestParam(required = false) String hostel) {
        List<AdminStudentResponseDTO> students = adminService.getStudents(hostel);
        return ResponseEntity.ok(new ApiResponse<>("Students fetched successfully", students));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) String status) {
        List<RoommateRequestResponseDTO> requests = adminService.getRequests(status);
        return ResponseEntity.ok(new ApiResponse<>("Requests fetched successfully", requests));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> assignRoommates(
            @Valid @RequestBody AdminAssignRequestDTO request) {
        RoommateRequestResponseDTO result = adminService.assignRoommates(request.getUserId1(), request.getUserId2());
        return ResponseEntity.ok(new ApiResponse<>("Roommates assigned successfully", result));
    }

    @PutMapping("/requests/{id}/respond")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> respondToRequest(
            @PathVariable Long id,
            @RequestParam String status) {
        RoommateRequestResponseDTO result = adminService.respondToRequest(id, status);
        return ResponseEntity.ok(new ApiResponse<>("Request " + status.toLowerCase() + " successfully", result));
    }

    /**
     * GET /admin/assignments?hostel=BIDHOLI_BOYS_HOSTEL
     * Returns all finalized room assignments.
     * Manager sees all; Warden sees only their hostel.
     */
    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<RoomAssignmentResponseDTO>>> getAssignments(
            @RequestParam(required = false) String hostel) {
        List<RoomAssignmentResponseDTO> assignments = adminService.getAssignments(hostel);
        return ResponseEntity.ok(new ApiResponse<>("Assignments fetched successfully", assignments));
    }
}

