package com.roomiematch.roomiematchai.controller;

import com.roomiematch.roomiematchai.dto.*;
import com.roomiematch.roomiematchai.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Manager API endpoints — top-level admin.
 * Protected: only MANAGER role can access.
 */
@RestController
@RequestMapping("/manager")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    /**
     * POST /manager/upload-students
     * Accepts a CSV or JSON file with student data.
     * Columns: name, sapId, universityEmail, password, hostelType
     */
    @PostMapping("/upload-students")
    public ResponseEntity<ApiResponse<UploadResultDTO>> uploadStudents(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "File is empty", null));
        }
        UploadResultDTO result = managerService.uploadStudents(file);
        return ResponseEntity.ok(new ApiResponse<>("Dataset uploaded successfully", result));
    }

    /**
     * POST /manager/assign-warden
     * Creates or elevates a user to WARDEN role for a specific hostel.
     */
    @PostMapping("/assign-warden")
    public ResponseEntity<ApiResponse<AdminStudentResponseDTO>> assignWarden(
            @Valid @RequestBody WardenAssignDTO dto) {
        AdminStudentResponseDTO result = managerService.assignWarden(dto);
        return ResponseEntity.ok(new ApiResponse<>("Warden assigned successfully", result));
    }

    /**
     * GET /manager/students?hostel=BIDHOLI_BOYS_HOSTEL
     * Returns all students in the organization, optionally filtered by hostel.
     */
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<AdminStudentResponseDTO>>> getStudents(
            @RequestParam(required = false) String hostel) {
        List<AdminStudentResponseDTO> students = managerService.getStudents(hostel);
        return ResponseEntity.ok(new ApiResponse<>("Students fetched successfully", students));
    }

    /**
     * GET /manager/wardens
     * Returns all wardens in the system.
     */
    @GetMapping("/wardens")
    public ResponseEntity<ApiResponse<List<AdminStudentResponseDTO>>> getWardens() {
        List<AdminStudentResponseDTO> wardens = managerService.getWardens();
        return ResponseEntity.ok(new ApiResponse<>("Wardens fetched successfully", wardens));
    }

    /**
     * GET /manager/requests?status=PENDING
     * Returns all roommate requests, optionally filtered by status.
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<RoommateRequestResponseDTO>>> getRequests(
            @RequestParam(required = false) String status) {
        List<RoommateRequestResponseDTO> requests = managerService.getRequests(status);
        return ResponseEntity.ok(new ApiResponse<>("Requests fetched successfully", requests));
    }

    /**
     * POST /manager/assign-roommates
     * body: { "userId1": 2, "userId2": 4 }
     */
    @PostMapping("/assign-roommates")
    public ResponseEntity<ApiResponse<RoommateRequestResponseDTO>> assignRoommates(
            @Valid @RequestBody AdminAssignRequestDTO request) {
        RoommateRequestResponseDTO result = managerService.assignRoommates(request.getUserId1(), request.getUserId2());
        return ResponseEntity.ok(new ApiResponse<>("Roommates assigned successfully", result));
    }
}
