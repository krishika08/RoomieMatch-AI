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

    // Admin: fetch all users in an organization
    List<User> findByOrganization(String organization);

    // Admin: fetch all users in an organization filtered by hostel
    List<User> findByOrganizationAndHostel(String organization, String hostel);

    // Admin: fetch users by role
    List<User> findByRole(Role role);
}
