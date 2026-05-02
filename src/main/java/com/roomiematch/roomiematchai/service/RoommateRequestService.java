package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.entity.RequestStatus;
import com.roomiematch.roomiematchai.entity.RoommateRequest;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoommateRequestRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoommateRequestService {

    private final RoommateRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContext;

    public RoommateRequestService(RoommateRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  AuthContextService authContext) {
        this.requestRepository = requestRepository;
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

        // Update status
        request.setStatus(newStatus);
        RoommateRequest updatedRequest = requestRepository.save(request);
        return new RoommateRequestResponseDTO(updatedRequest);
    }
}
