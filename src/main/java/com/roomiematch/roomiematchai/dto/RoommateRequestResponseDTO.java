package com.roomiematch.roomiematchai.dto;

import com.roomiematch.roomiematchai.entity.RoommateRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoommateRequestResponseDTO {

    private Long id;
    private Long senderId;
    private String senderEmail;
    private Long receiverId;
    private String receiverEmail;
    private String status;
    private LocalDateTime createdAt;

    // Constructor from entity — keeps conversion logic in one place
    public RoommateRequestResponseDTO(RoommateRequest request) {
        this.id = request.getId();
        this.senderId = request.getSender().getId();
        this.senderEmail = request.getSender().getEmail();
        this.receiverId = request.getReceiver().getId();
        this.receiverEmail = request.getReceiver().getEmail();
        this.status = request.getStatus().name();
        this.createdAt = request.getCreatedAt();
    }
}
