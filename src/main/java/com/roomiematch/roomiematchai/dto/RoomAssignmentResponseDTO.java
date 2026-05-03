package com.roomiematch.roomiematchai.dto;

import com.roomiematch.roomiematchai.entity.RoomAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for room assignment responses shown in the admin/warden dashboard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomAssignmentResponseDTO {

    private Long id;
    private Long user1Id;
    private String user1Name;
    private String user1Email;
    private Long user2Id;
    private String user2Name;
    private String user2Email;
    private String hostel;
    private String assignedByEmail;
    private LocalDateTime assignedAt;
    private String status;

    /** Convenience constructor from entity. */
    public RoomAssignmentResponseDTO(RoomAssignment a) {
        this.id = a.getId();
        this.user1Id = a.getUser1().getId();
        this.user1Name = a.getUser1().getName();
        this.user1Email = a.getUser1().getEmail();
        this.user2Id = a.getUser2().getId();
        this.user2Name = a.getUser2().getName();
        this.user2Email = a.getUser2().getEmail();
        this.hostel = a.getHostel();
        this.assignedByEmail = a.getAssignedBy().getEmail();
        this.assignedAt = a.getAssignedAt();
        this.status = a.getStatus().name();
    }
}
