package com.roomiematch.roomiematchai.repository;

import com.roomiematch.roomiematchai.entity.RequestStatus;
import com.roomiematch.roomiematchai.entity.RoommateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoommateRequestRepository extends JpaRepository<RoommateRequest, Long> {

    // Fetch all requests received by a user
    List<RoommateRequest> findByReceiverId(Long receiverId);

    // Fetch all requests sent by a user
    List<RoommateRequest> findBySenderId(Long senderId);

    // Check if a pending request already exists between sender and receiver (prevents duplicates)
    Optional<RoommateRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, RequestStatus status);
}
