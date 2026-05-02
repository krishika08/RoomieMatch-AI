package com.roomiematch.roomiematchai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "roommate_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoommateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the request
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // The user who receives the request
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
