package com.roomiematch.roomiematchai.repository;

import com.roomiematch.roomiematchai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
