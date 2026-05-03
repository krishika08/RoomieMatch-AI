package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.AdminAssignRequestDTO;
import com.roomiematch.roomiematchai.dto.AdminStudentResponseDTO;
import com.roomiematch.roomiematchai.dto.ApiResponse;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin API endpoints.
 * Protected by Spring Security — only ADMIN and HOSTEL_ADMIN roles can access.
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'HOSTEL_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * GET /admin/students
     * GET /admin/students?hostel=BIDHOLI_BOYS_HOSTEL
     *
     * Returns all students in the admin's organization.
     * Super admin can filter by hostel; hostel admin auto-filters to their hostel.
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<AdminStudentResponseDTO>>> getStudents(
            @RequestParam(required = false) String hostel) {
        List<AdminStudentResponseDTO> students = adminService.getStudents(hostel);
        return ResponseEntity.ok(new ApiResponse<>("Students fetched successfully", students));
    }

    /**
     * GET /admin/requests
     * GET /admin/requests?status=PENDING
     *
     * Returns all roommate requests, optionally filtered by status.
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) String status) {
        List<RoommateRequestResponseDTO> requests = adminService.getRequests(status);
        return ResponseEntity.ok(new ApiResponse<>("Requests fetched successfully", requests));
    }

    /**
     * POST /admin/assign
     * body: { "userId1": 2, "userId2": 4 }
     *
     * Manually assigns two users as roommates (auto-accepted request).
     * Both users must be in the same organization AND same hostel.
     */
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> assignRoommates(
            @Valid @RequestBody AdminAssignRequestDTO request) {
        RoommateRequestResponseDTO result = adminService.assignRoommates(request.getUserId1(), request.getUserId2());
        return ResponseEntity.ok(new ApiResponse<>("Roommates assigned successfully", result));
    }
}
