package com.roomiematch.roomiematchai.repository;

import com.roomiematch.roomiematchai.entity.Role;
import com.roomiematch.roomiematchai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user by email (used for duplicate registration checks)
    Optional<User> findByEmail(String email);

    // Finds a user by SAP ID
    Optional<User> findBySapId(String sapId);

    // Fetch all users in an organization
    List<User> findByOrganization(String organization);

    // Fetch all users in an organization filtered by hostel
    List<User> findByOrganizationAndHostel(String organization, String hostel);

    // Fetch all users in an organization, hostel, and specific role
    List<User> findByOrganizationAndHostelAndRole(String organization, String hostel, Role role);

    // Fetch users by role
    List<User> findByRole(Role role);

    // Fetch users created by a specific admin
    List<User> findByCreatedBy(Long createdById);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM student_profiles", nativeQuery = true)
    void clearProfiles();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM roommate_requests", nativeQuery = true)
    void clearRequests();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "ALTER TABLE users MODIFY COLUMN role VARCHAR(50)", nativeQuery = true)
    void alterRoleColumn();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM users", nativeQuery = true)
    void deleteAllNative();
}
