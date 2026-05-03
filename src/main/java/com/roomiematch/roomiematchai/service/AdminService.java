package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.AdminStudentResponseDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.entity.*;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
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
 * Service layer for admin operations.
 * Supports two admin types:
 *   - ADMIN (super admin): can see ALL users across ALL hostels in the organization
 *   - HOSTEL_ADMIN: can only see users in their assigned hostel
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RoommateRequestRepository requestRepository;
    private final StudentProfileRepository profileRepository;
    private final AuthContextService authContext;

    public AdminService(UserRepository userRepository,
                        RoommateRequestRepository requestRepository,
                        StudentProfileRepository profileRepository,
                        AuthContextService authContext) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
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

        if (admin.getRole() == Role.HOSTEL_ADMIN) {
            // Hostel admin can only see users in their own hostel
            users = userRepository.findByOrganizationAndHostel(admin.getOrganization(), admin.getHostel());
        } else if (hostelFilter != null && !hostelFilter.isBlank()) {
            // Super admin with hostel filter
            users = userRepository.findByOrganizationAndHostel(admin.getOrganization(), hostelFilter.toUpperCase());
        } else {
            // Super admin — all users in the organization
            users = userRepository.findByOrganization(admin.getOrganization());
        }

        // Exclude admin users from the student list
        return users.stream()
                .filter(u -> u.getRole() == Role.USER)
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

        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                RequestStatus status = RequestStatus.valueOf(statusFilter.toUpperCase());
                requests = requestRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid status filter. Use: PENDING, ACCEPTED, REJECTED");
            }
        } else {
            requests = requestRepository.findAll();
        }

        // For hostel admins, filter to only requests involving users in their hostel
        if (admin.getRole() == Role.HOSTEL_ADMIN) {
            String adminHostel = admin.getHostel();
            requests = requests.stream()
                    .filter(r -> adminHostel.equals(r.getSender().getHostel())
                              || adminHostel.equals(r.getReceiver().getHostel()))
                    .collect(Collectors.toList());
        }

        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  3. Manually assign roommates
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

        // Validation: same organization
        if (!user1.getOrganization().equals(user2.getOrganization())) {
            throw new IllegalStateException("Cannot assign roommates from different organizations.");
        }

        // Validation: same hostel
        if (!user1.getHostel().equals(user2.getHostel())) {
            throw new IllegalStateException("Cannot assign roommates from different hostels.");
        }

        // Hostel admin can only assign within their hostel
        if (admin.getRole() == Role.HOSTEL_ADMIN && !admin.getHostel().equals(user1.getHostel())) {
            throw new IllegalStateException("You can only assign roommates within your hostel.");
        }

        // Check for existing pending/accepted request between these users (either direction)
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId1, userId2, RequestStatus.PENDING)
                .ifPresent(r -> { throw new IllegalStateException("A pending request already exists between these users."); });
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId2, userId1, RequestStatus.PENDING)
                .ifPresent(r -> { throw new IllegalStateException("A pending request already exists between these users."); });
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId1, userId2, RequestStatus.ACCEPTED)
                .ifPresent(r -> { throw new IllegalStateException("These users are already assigned as roommates."); });
        requestRepository.findBySenderIdAndReceiverIdAndStatus(userId2, userId1, RequestStatus.ACCEPTED)
                .ifPresent(r -> { throw new IllegalStateException("These users are already assigned as roommates."); });

        // Create an auto-accepted request
        RoommateRequest request = new RoommateRequest();
        request.setSender(user1);
        request.setReceiver(user2);
        request.setStatus(RequestStatus.ACCEPTED);

        RoommateRequest saved = requestRepository.save(request);
        log.info("Admin assigned roommates: {} <-> {} (request #{})", user1.getEmail(), user2.getEmail(), saved.getId());

        return new RoommateRequestResponseDTO(saved);
    }

    // ──────────────────────────────────────────────
    //  Helper: Convert User to AdminStudentResponseDTO
    // ──────────────────────────────────────────────
    private AdminStudentResponseDTO toStudentDTO(User user) {
        boolean hasProfile = profileRepository.findByUserId(user.getId()).isPresent();
        return new AdminStudentResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getOrganization(),
                user.getHostel(),
                hasProfile,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
