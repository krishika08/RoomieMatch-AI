# Strict Audit Report: RoomieMatch AI Hierarchical Admin System

As a Senior Backend + System Design Reviewer, I have performed a rigorous, production-level validation of the Hierarchical Admin System. The architecture successfully transitions the platform from a flat structure to a robust, multi-tenant hierarchy. 

Below is the comprehensive audit evaluating correctness, security, and edge-case handling.

---

### 1. What is PERFECT ✅

* **Database Schema Integration:** The `User` entity seamlessly integrates `sapId`, `createdBy`, `role` (EnumType), and `hostel` without requiring a destructive redesign of the core system.
* **Warden Auto-Scoping:** Wardens are hard-scoped to their assigned hostels. Queries like `userRepository.findByOrganizationAndHostel(org, warden.getHostel())` inherently prevent IDOR (Insecure Direct Object Reference) vulnerabilities. Wardens cannot "hack" the URL to view other hostels.
* **Role Inheritance for Sub-Wardens:** When a Warden creates a sub-warden (`POST /warden/create`), the system forcefully ignores any user input for the hostel and rigidly copies the creator's hostel (`newWarden.setHostel(currentWarden.getHostel())`). This is highly secure.
* **Spring Security Enforcement:** JWT parsing correctly sets the `GrantedAuthority`, and method-level `@PreAuthorize("hasRole('MANAGER')")` guarantees endpoints are hermetically sealed.

---

### 2. What is PARTIALLY WORKING ⚠️

* **Upload Dataset Transaction Scope:** The `POST /manager/upload-students` endpoint loops through the CSV/JSON and saves users sequentially inside a single `@Transactional` method.
  * *Why this is a risk:* If an upload contains 5,000 students, an exception on the 4,999th student will roll back the entire batch. Furthermore, large synchronous uploads can cause HTTP timeouts.
* **Hostel String Normalization:** The `ManagerService.normalizeHostel()` attempts to clean up user input (e.g., "Bidholi Boys" → "BIDHOLI_BOYS_HOSTEL"). 
  * *Why this is a risk:* It relies on string parsing `upper.contains("BOYS")`. If a manager uploads "Bidholi South Wing", the parser might fail to assign them to a valid hostel enum equivalent, potentially isolating those students.

---

### 3. What is BROKEN ❌

* **Elevating Students to Wardens (Orphan Data Leak):** In `ManagerService.assignWarden()`, if a manager elevates an existing `STUDENT` account to a `WARDEN`, the user role is updated perfectly. However, the system **forgets to delete** their `StudentProfile` and any pending/accepted `RoommateRequests`. 
  * *Consequence:* A Warden might still appear in the matching pool for other students, or retain an accepted roommate, breaking the logical separation of staff and students.

---

### 4. Critical Fixes Required 🔧

1. **Purge Student Data on Elevation:**
   When `assignWarden` elevates a student, the logic must forcefully invoke `profileRepository.deleteByUserId()` and resolve/delete their `RoommateRequests`.
2. **Handle Unique Constraint Violations Gracefully:**
   While the dataset upload skips existing emails and `sapId`s, a concurrent registration (race condition) could trigger a `DataIntegrityViolationException` at the DB level, crashing the upload transaction. Catch block needs to specifically handle `DataIntegrityViolationException`.

---

### 5. Improvements for Production 🚀

1. **Async Batch Processing:** For production, `upload-students` should accept the file, return a `202 Accepted` with a `jobId`, and process the CSV asynchronously (e.g., using Spring Batch or `@Async`).
2. **Force Password Reset:** Currently, the Manager assigns plain-text passwords in the CSV which are hashed. In production, the CSV shouldn't contain passwords. The system should generate a random password/token and trigger an email: *"Welcome to RoomieMatch! Click here to set your password."*
3. **Audit Logging:** You added `createdBy`. To be fully compliant, you should add a dedicated `audit_logs` table to track *actions*: e.g., `"Warden [A] accepted request between [X] and [Y] on [Date]"`.

---

### 6. Final Rating

**8.5 / 10 — Excellent Foundation**

**Verdict:** The system is completely demo-ready and highly functional. The core security domains are perfectly isolated. It loses 1.5 points strictly due to the lack of async batch uploading for massive datasets and the edge-case of orphan data when elevating a student to a warden. For a college project or MVP, this is exceptionally well-engineered.
