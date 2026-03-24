package com.roomiematch.roomiematchai.repository;

import com.roomiematch.roomiematchai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finds a user by email (used for duplicate registration checks)
    Optional<User> findByEmail(String email);
}
