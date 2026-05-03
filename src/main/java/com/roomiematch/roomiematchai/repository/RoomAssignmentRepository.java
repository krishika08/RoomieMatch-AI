package com.roomiematch.roomiematchai.repository;

import com.roomiematch.roomiematchai.entity.AssignmentStatus;
import com.roomiematch.roomiematchai.entity.RoomAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, Long> {

    /** All assignments for a specific hostel. */
    List<RoomAssignment> findByHostel(String hostel);

    /** All assignments filtered by hostel and status. */
    List<RoomAssignment> findByHostelAndStatus(String hostel, AssignmentStatus status);

    /** Check if a user is already part of an active assignment (as user1 or user2). */
    @Query("SELECT COUNT(a) > 0 FROM RoomAssignment a " +
           "WHERE a.status = 'ASSIGNED' AND (a.user1.id = :userId OR a.user2.id = :userId)")
    boolean isUserAssigned(@Param("userId") Long userId);

    /** Find all active assignments involving a specific user. */
    @Query("SELECT a FROM RoomAssignment a " +
           "WHERE a.status = 'ASSIGNED' AND (a.user1.id = :userId OR a.user2.id = :userId)")
    List<RoomAssignment> findActiveByUserId(@Param("userId") Long userId);

    /** All active assignments. */
    List<RoomAssignment> findByStatus(AssignmentStatus status);
}
