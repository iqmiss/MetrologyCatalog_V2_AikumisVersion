package com.catalog.repository;

import com.catalog.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    long countByRole(String role);
    Optional<User> findByPasswordResetToken(String token);
}