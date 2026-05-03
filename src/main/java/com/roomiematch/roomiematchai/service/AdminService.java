package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.AdminStudentResponseDTO;
import com.roomiematch.roomiematchai.dto.RoomAssignmentResponseDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.entity.*;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoomAssignmentRepository;
import com.roomiematch.roomiematchai.repository.RoommateRequestRepository;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Legacy admin service — kept for backward compatibility with /admin/** endpoints.
 * Now uses MANAGER/WARDEN roles instead of ADMIN/HOSTEL_ADMIN.
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RoommateRequestRepository requestRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final StudentProfileRepository profileRepository;
    private final AuthContextService authContext;

    public AdminService(UserRepository userRepository,
                        RoommateRequestRepository requestRepository,
                        RoomAssignmentRepository assignmentRepository,
                        StudentProfileRepository profileRepository,
                        AuthContextService authContext) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.assignmentRepository = assignmentRepository;
        this.profileRepository = profileRepository;
        this.authContext = authContext;
    }

    // ──────────────────────────────────────────────
    //  1. Get students (filtered by admin's scope)
    // ──────────────────────────────────────────────
    public List<AdminStudentResponseDTO> getStudents(String hostelFilter) {
        User admin = authContext.getLoggedInUser();
        log.info("Admin {} fetching students, hostel filter: {}", admin.getEmail(), hostelFilter);

        List<User> users;

        if (admin.getRole() == Role.WARDEN) {
            // Warden can only see users in their own hostel
            users = userRepository.findByOrganizationAndHostel(admin.getOrganization(), admin.getHostel());
        } else if (hostelFilter != null && !hostelFilter.isBlank()) {
            // Manager with hostel filter
            users = userRepository.findByOrganizationAndHostel(admin.getOrganization(), hostelFilter.toUpperCase());
        } else {
            // Manager — all users in the organization
            users = userRepository.findByOrganization(admin.getOrganization());
        }

        // Exclude admin/warden users from the student list
        return users.stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .map(this::toStudentDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  2. Get all roommate requests
    // ──────────────────────────────────────────────
    public List<RoommateRequestResponseDTO> getRequests(String statusFilter) {
        User admin = authContext.getLoggedInUser();
        log.info("Admin {} fetching requests, status filter: {}", admin.getEmail(), statusFilter);

        List<RoommateRequest> requests;
        boolean isWarden = admin.getRole() == Role.WARDEN;
        String adminHostel = admin.getHostel();

        RequestStatus status = null;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                status = RequestStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid status filter. Use: PENDING, ACCEPTED, REJECTED");
            }
        }

        if (isWarden && adminHostel != null) {
            requests = (status != null)
                    ? requestRepository.findByHostelAndStatus(adminHostel, status)
                    : requestRepository.findByHostel(adminHostel);
        } else {
            requests = (status != null)
                    ? requestRepository.findByStatus(status)
                    : requestRepository.findAll();
        }

        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  3. Manually assign roommates (creates request + assignment)
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO assignRoommates(Long userId1, Long userId2) {
        User admin = authContext.getLoggedInUser();
        log.info("Admin {} assigning roommates: {} <-> {}", admin.getEmail(), userId1, userId2);

        if (userId1.equals(userId2)) {
            throw new IllegalStateException("Cannot assign a user as their own roommate.");
        }

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId2));

        if (!user1.getOrganization().equals(user2.getOrganization())) {
            throw new IllegalStateException("Cannot assign roommates from different organizations.");
        }
        if (!user1.getHostel().equals(user2.getHostel())) {
            throw new IllegalStateException("Cannot assign roommates from different hostels.");
        }
        if (admin.getRole() == Role.WARDEN && !admin.getHostel().equals(user1.getHostel())) {
            throw new IllegalStateException("You can only assign roommates within your hostel.");
        }

        // Check if either user is already assigned
        if (assignmentRepository.isUserAssigned(userId1)) {
            throw new IllegalStateException("User " + user1.getEmail() + " is already assigned a roommate.");
        }
        if (assignmentRepository.isUserAssigned(userId2)) {
            throw new IllegalStateException("User " + user2.getEmail() + " is already assigned a roommate.");
        }

        // Check for existing pending/accepted requests
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId1, userId2, RequestStatus.PENDING)
                .ifPresent(r -> { throw new IllegalStateException("A pending request already exists between these users."); });
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId2, userId1, RequestStatus.PENDING)
                .ifPresent(r -> { throw new IllegalStateException("A pending request already exists between these users."); });

        // Create the accepted request
        RoommateRequest request = new RoommateRequest();
        request.setSender(user1);
        request.setReceiver(user2);
        request.setStatus(RequestStatus.ACCEPTED);
        RoommateRequest saved = requestRepository.save(request);

        // Create the final room assignment
        createAssignment(user1, user2, admin);

        log.info("Admin assigned roommates: {} <-> {} (request #{}, assignment created)",
                user1.getEmail(), user2.getEmail(), saved.getId());

        return new RoommateRequestResponseDTO(saved);
    }

    // ──────────────────────────────────────────────
    //  4. Admin respond to a request (accept/reject)
    //     Auto-creates assignment on ACCEPTED
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO respondToRequest(Long requestId, String statusStr) {
        User admin = authContext.getLoggedInUser();

        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid status. Use ACCEPTED or REJECTED.");
        }
        if (newStatus == RequestStatus.PENDING) {
            throw new IllegalStateException("Invalid status. Use ACCEPTED or REJECTED.");
        }

        RoommateRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Roommate request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been " + request.getStatus().name().toLowerCase() + ".");
        }

        if (admin.getRole() == Role.WARDEN) {
            String adminHostel = admin.getHostel();
            boolean senderInHostel = adminHostel != null && adminHostel.equals(request.getSender().getHostel());
            boolean receiverInHostel = adminHostel != null && adminHostel.equals(request.getReceiver().getHostel());
            if (!senderInHostel && !receiverInHostel) {
                throw new IllegalStateException("You can only manage requests within your hostel.");
            }
        }

        // If accepting, check both users aren't already assigned
        if (newStatus == RequestStatus.ACCEPTED) {
            Long senderId = request.getSender().getId();
            Long receiverId = request.getReceiver().getId();
            if (assignmentRepository.isUserAssigned(senderId)) {
                throw new IllegalStateException("User " + request.getSender().getEmail() + " is already assigned a roommate.");
            }
            if (assignmentRepository.isUserAssigned(receiverId)) {
                throw new IllegalStateException("User " + request.getReceiver().getEmail() + " is already assigned a roommate.");
            }
        }

        request.setStatus(newStatus);
        RoommateRequest updated = requestRepository.save(request);

        // Auto-create room assignment when request is accepted
        if (newStatus == RequestStatus.ACCEPTED) {
            createAssignment(request.getSender(), request.getReceiver(), admin);
            log.info("Auto-created room assignment for accepted request #{}", requestId);
        }

        return new RoommateRequestResponseDTO(updated);
    }

    // ──────────────────────────────────────────────
    //  5. Get all room assignments (role-aware)
    // ──────────────────────────────────────────────
    public List<RoomAssignmentResponseDTO> getAssignments(String hostelFilter) {
        User admin = authContext.getLoggedInUser();
        log.info("Admin {} fetching assignments, hostel filter: {}", admin.getEmail(), hostelFilter);

        List<RoomAssignment> assignments;

        if (admin.getRole() == Role.WARDEN) {
            // Warden sees only their hostel
            assignments = assignmentRepository.findByHostel(admin.getHostel());
        } else if (hostelFilter != null && !hostelFilter.isBlank()) {
            assignments = assignmentRepository.findByHostel(hostelFilter.toUpperCase());
        } else {
            assignments = assignmentRepository.findAll();
        }

        return assignments.stream()
                .map(RoomAssignmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────
    private void createAssignment(User user1, User user2, User assignedBy) {
        RoomAssignment assignment = new RoomAssignment();
        assignment.setUser1(user1);
        assignment.setUser2(user2);
        assignment.setHostel(user1.getHostel());
        assignment.setAssignedBy(assignedBy);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        assignmentRepository.save(assignment);
    }

    private AdminStudentResponseDTO toStudentDTO(User user) {
        boolean hasProfile = profileRepository.findByUserId(user.getId()).isPresent();
        return new AdminStudentResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSapId(),
                user.getRole().name(),
                user.getOrganization(),
                user.getHostel(),
                hasProfile,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
