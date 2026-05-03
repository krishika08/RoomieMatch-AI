package com.roomiematch.roomiematchai.entity;

public enum Role {
    USER,
    ADMIN,         // Super admin — manages all hostels in the organization
    HOSTEL_ADMIN   // Per-hostel admin — manages only their assigned hostel
}
