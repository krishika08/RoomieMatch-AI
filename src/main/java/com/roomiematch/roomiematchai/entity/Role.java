package com.roomiematch.roomiematchai.entity;

/**
 * Hierarchical role system for RoomieMatch AI.
 *
 *   MANAGER  → Top-level admin (Hostel Manager). Full control over all hostels.
 *   WARDEN   → Mid-level admin (Hostel Warden). Manages a single assigned hostel.
 *   STUDENT  → Regular user. Can create profiles, view matches, send requests.
 */
public enum Role {
    STUDENT,
    WARDEN,
    MANAGER
}
