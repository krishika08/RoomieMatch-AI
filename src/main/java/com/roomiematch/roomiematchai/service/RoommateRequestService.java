package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.entity.*;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoomAssignmentRepository;
import com.roomiematch.roomiematchai.repository.RoommateRequestRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoommateRequestService {

    private final RoommateRequestRepository requestRepository;
    private final RoomAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContext;

    public RoommateRequestService(RoommateRequestRepository requestRepository,
                                  RoomAssignmentRepository assignmentRepository,
                                  UserRepository userRepository,
                                  AuthContextService authContext) {
        this.requestRepository = requestRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.authContext = authContext;
    }

    // ──────────────────────────────────────────────
    //  1. Send a Roommate Request
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO sendRequest(Long receiverId) {
        User sender = authContext.getLoggedInUser();

        // Validation: Cannot send request to yourself
        if (sender.getId().equals(receiverId)) {
            throw new IllegalStateException("You cannot send a roommate request to yourself.");
        }

        // Fetch receiver — throws 404 if not found
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with id: " + receiverId));

        // Validation: Sender must not already be assigned
        if (assignmentRepository.isUserAssigned(sender.getId())) {
            throw new IllegalStateException("You are already assigned a roommate. No further requests allowed.");
        }

        // Validation: Receiver must not already be assigned
        if (assignmentRepository.isUserAssigned(receiverId)) {
            throw new IllegalStateException("This user is already assigned a roommate.");
        }

        // Validation: Must be in the same hostel
        if (sender.getHostel() != null && receiver.getHostel() != null
                && !sender.getHostel().equalsIgnoreCase(receiver.getHostel())) {
            throw new IllegalStateException("You can only send roommate requests to users in your hostel.");
        }

        // Validation: Must be in the same organization
        if (sender.getOrganization() != null && receiver.getOrganization() != null
                && !sender.getOrganization().equalsIgnoreCase(receiver.getOrganization())) {
            throw new IllegalStateException("You can only send roommate requests to users in your organization.");
        }

        // Validation: Prevent duplicate pending requests (A→B)
        requestRepository.findBySenderIdAndReceiverIdAndStatus(sender.getId(), receiverId, RequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalStateException("A pending request already exists for this user.");
                });

        // Validation: Prevent reverse duplicate pending requests (B→A)
        requestRepository.findBySenderIdAndReceiverIdAndStatus(receiverId, sender.getId(), RequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalStateException("This user has already sent you a pending request. Check your incoming requests.");
                });

        // Create and save the request
        RoommateRequest request = new RoommateRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(RequestStatus.PENDING);

        RoommateRequest savedRequest = requestRepository.save(request);
        return new RoommateRequestResponseDTO(savedRequest);
    }

    // ──────────────────────────────────────────────
    //  2. Get Incoming Requests (where I am the receiver)
    // ──────────────────────────────────────────────
    public List<RoommateRequestResponseDTO> getIncomingRequests() {
        User user = authContext.getLoggedInUser();
        List<RoommateRequest> requests = requestRepository.findByReceiverId(user.getId());
        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  3. Get Sent Requests (where I am the sender)
    // ──────────────────────────────────────────────
    public List<RoommateRequestResponseDTO> getSentRequests() {
        User user = authContext.getLoggedInUser();
        List<RoommateRequest> requests = requestRepository.findBySenderId(user.getId());
        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  4. Respond to a Request (Accept / Reject)
    //     Auto-creates RoomAssignment on ACCEPTED
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO respondToRequest(Long requestId, String status) {
        User user = authContext.getLoggedInUser();

        // Parse the status string into the enum
        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid status. Use ACCEPTED or REJECTED.");
        }

        // Only ACCEPTED or REJECTED are valid responses
        if (newStatus == RequestStatus.PENDING) {
            throw new IllegalStateException("Invalid status. Use ACCEPTED or REJECTED.");
        }

        // Fetch the request
        RoommateRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Roommate request not found with id: " + requestId));

        // Only the receiver can respond to a request
        if (!request.getReceiver().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only respond to requests sent to you.");
        }

        // Can only respond to PENDING requests
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been " + request.getStatus().name().toLowerCase() + ".");
        }

        // If accepting, verify neither user is already assigned
        if (newStatus == RequestStatus.ACCEPTED) {
            if (assignmentRepository.isUserAssigned(request.getSender().getId())) {
                throw new IllegalStateException("The sender is already assigned a roommate.");
            }
            if (assignmentRepository.isUserAssigned(request.getReceiver().getId())) {
                throw new IllegalStateException("You are already assigned a roommate.");
            }
        }

        // Update status
        request.setStatus(newStatus);
        RoommateRequest updatedRequest = requestRepository.save(request);

        // Auto-create room assignment when student accepts
        if (newStatus == RequestStatus.ACCEPTED) {
            RoomAssignment assignment = new RoomAssignment();
            assignment.setUser1(request.getSender());
            assignment.setUser2(request.getReceiver());
            assignment.setHostel(request.getSender().getHostel());
            assignment.setAssignedBy(user); // the student who accepted
            assignment.setStatus(AssignmentStatus.ASSIGNED);
            assignmentRepository.save(assignment);
        }

        return new RoommateRequestResponseDTO(updatedRequest);
    }
}
