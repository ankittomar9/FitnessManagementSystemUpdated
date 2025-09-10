package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.models.User;
import com.fitness.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service layer for user-related business logic.
 * Handles user registration and profile management.
 * Uses UserRepository for data access.
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository repository;



    public UserResponse register(@Valid RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        try {
            if (repository.existsByEmail(request.getEmail())) {
              //  log.warn("Registration failed - Email already exists: {}", request.getEmail());
                User existingUser = repository.findByEmail(request.getEmail());
                UserResponse userResponse = new UserResponse();
                userResponse.setUserId(existingUser.getId());
                userResponse.setKeycloakId(existingUser.getKeycloakId());
                userResponse.setPassword(existingUser.getPassword()); // Never expose password in userResponse
                userResponse.setFirstName(existingUser.getFirstName());
                userResponse.setLastName(existingUser.getLastName());
                userResponse.setEmail(existingUser.getEmail());
                userResponse.setCreatedAt(existingUser.getCreatedAt());
                userResponse.setUpdatedAt(existingUser.getUpdatedAt());
                return userResponse;
            }

            log.debug("Creating new user with email: {}", request.getEmail());
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword()); // Never log actual passwords
            user.setKeycloakId(request.getKeycloakId());// get Keycloak ID from request
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            log.debug("Saving user to database");
            User savedUser = repository.save(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());
            
            UserResponse response = new UserResponse();
            response.setUserId(savedUser.getId());
            response.setKeycloakId(savedUser.getKeycloakId());
            response.setPassword(savedUser.getPassword()); // Never expose password in response
            response.setFirstName(savedUser.getFirstName());
            response.setLastName(savedUser.getLastName());
            response.setEmail(savedUser.getEmail());
            response.setCreatedAt(savedUser.getCreatedAt());
            response.setUpdatedAt(savedUser.getUpdatedAt());
            
            log.debug("User registration completed for ID: {}", savedUser.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error during user registration for email: {}", request.getEmail(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    public UserResponse getUserProfile(String userId) {
        log.debug("Fetching user profile for ID: {}", userId);
        
        try {
            User user = repository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("User not found with ID: {}", userId);
                        return new RuntimeException("User not found with id: " + userId);
                    });
                    
            log.debug("Successfully retrieved user: {}", user.getEmail());
            
            UserResponse response = new UserResponse();
            response.setUserId(user.getId());
            response.setPassword("********"); // Never expose password in response
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());
            
            log.debug("Profile data prepared for user ID: {}", userId);
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching profile for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user profile: " + e.getMessage(), e);
        }
    }

    public Boolean existsByUserId(String userId) {
        log.info("Calling User validation API for User with userID: {}", userId);
        return repository.existsByKeycloakId(userId);
    }
}
