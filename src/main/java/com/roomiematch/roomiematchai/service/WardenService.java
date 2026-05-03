package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.AdminStudentResponseDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.dto.WardenCreateDTO;
import com.roomiematch.roomiematchai.entity.*;
import com.roomiematch.roomiematchai.exception.DuplicateEmailException;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoommateRequestRepository;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for WARDEN-level operations.
 * All operations are automatically scoped to the warden's assigned hostel.
 */
@Service
public class WardenService {

    private static final Logger log = LoggerFactory.getLogger(WardenService.class);

    private final UserRepository userRepository;
    private final RoommateRequestRepository requestRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthContextService authContext;

    public WardenService(UserRepository userRepository,
                         RoommateRequestRepository requestRepository,
                         StudentProfileRepository profileRepository,
                         PasswordEncoder passwordEncoder,
                         AuthContextService authContext) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authContext = authContext;
    }

    // ──────────────────────────────────────────────
    //  1. Get students in warden's hostel
    // ──────────────────────────────────────────────
    public List<AdminStudentResponseDTO> getStudents() {
        User warden = authContext.getLoggedInUser();
        String org = warden.getOrganization() != null ? warden.getOrganization() : "UPES";

        List<User> users = userRepository.findByOrganizationAndHostel(org, warden.getHostel());

        return users.stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .map(this::toStudentDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  2. Create a sub-warden for the same hostel
    // ──────────────────────────────────────────────
    @Transactional
    public AdminStudentResponseDTO createWarden(WardenCreateDTO dto) {
        User currentWarden = authContext.getLoggedInUser();
        log.info("Warden {} creating sub-warden: {}", currentWarden.getEmail(), dto.getEmail());

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException(dto.getEmail());
        }

        User newWarden = new User();
        newWarden.setName(dto.getName());
        newWarden.setEmail(dto.getEmail());
        newWarden.setPassword(passwordEncoder.encode(dto.getPassword()));
        newWarden.setRole(Role.WARDEN);
        newWarden.setOrganization(currentWarden.getOrganization());
        newWarden.setHostel(currentWarden.getHostel()); // same hostel as creator
        newWarden.setCreatedBy(currentWarden.getId());

        User saved = userRepository.save(newWarden);
        log.info("Sub-warden created: {} for hostel {}", saved.getEmail(), saved.getHostel());

        return toStudentDTO(saved);
    }

    // ──────────────────────────────────────────────
    //  3. Get requests in warden's hostel
    // ──────────────────────────────────────────────
    public List<RoommateRequestResponseDTO> getRequests(String statusFilter) {
        User warden = authContext.getLoggedInUser();
        String hostel = warden.getHostel();

        List<RoommateRequest> requests;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                RequestStatus status = RequestStatus.valueOf(statusFilter.toUpperCase());
                requests = requestRepository.findByHostelAndStatus(hostel, status);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid status. Use: PENDING, ACCEPTED, REJECTED");
            }
        } else {
            requests = requestRepository.findByHostel(hostel);
        }

        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  4. Respond to a request (warden scope)
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO respondToRequest(Long requestId, String statusStr) {
        User warden = authContext.getLoggedInUser();

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
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been " + request.getStatus().name().toLowerCase() + ".");
        }

        // Scope check: request must involve users from warden's hostel
        String wardenHostel = warden.getHostel();
        boolean senderInHostel = wardenHostel != null && wardenHostel.equals(request.getSender().getHostel());
        boolean receiverInHostel = wardenHostel != null && wardenHostel.equals(request.getReceiver().getHostel());
        if (!senderInHostel && !receiverInHostel) {
            throw new IllegalStateException("You can only manage requests within your hostel.");
        }

        request.setStatus(newStatus);
        RoommateRequest updated = requestRepository.save(request);
        log.info("Warden {} {} request #{}", warden.getEmail(), newStatus, requestId);

        return new RoommateRequestResponseDTO(updated);
    }

    // ──────────────────────────────────────────────
    //  Helper
    // ──────────────────────────────────────────────
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
