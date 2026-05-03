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

    // Admin: fetch all requests by status
    List<RoommateRequest> findByStatus(RequestStatus status);

    // Admin: fetch requests involving users in a specific hostel
    @org.springframework.data.jpa.repository.Query(
        "SELECT r FROM RoommateRequest r WHERE r.sender.hostel = :hostel OR r.receiver.hostel = :hostel")
    List<RoommateRequest> findByHostel(@org.springframework.data.repository.query.Param("hostel") String hostel);

    // Admin: fetch requests involving users in a specific hostel AND with a specific status
    @org.springframework.data.jpa.repository.Query(
        "SELECT r FROM RoommateRequest r WHERE (r.sender.hostel = :hostel OR r.receiver.hostel = :hostel) AND r.status = :status")
    List<RoommateRequest> findByHostelAndStatus(
        @org.springframework.data.repository.query.Param("hostel") String hostel,
        @org.springframework.data.repository.query.Param("status") RequestStatus status);
}
