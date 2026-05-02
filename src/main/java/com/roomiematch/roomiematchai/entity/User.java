package com.roomiematch.roomiematchai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}
