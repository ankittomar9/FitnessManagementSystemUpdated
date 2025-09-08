package com.fitness.userservice.repository;

import com.fitness.userservice.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access layer for User entities.
 * Extends JpaRepository for CRUD operations.
 * Includes custom query methods for user-specific data access.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email);

    Boolean existsByKeycloakId(String userId);
}
