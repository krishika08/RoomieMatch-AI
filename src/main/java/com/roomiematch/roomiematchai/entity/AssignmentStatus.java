package com.roomiematch.roomiematchai.entity;

/**
 * Status for final room assignments.
 *   ASSIGNED  → Pair is locked, no further requests allowed.
 *   CANCELLED → Assignment was revoked by admin.
 */
public enum AssignmentStatus {
    ASSIGNED,
    CANCELLED
}
