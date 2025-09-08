package com.fitness.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * Service class for handling user-related operations in the API Gateway.
 * Acts as a client to the User Service microservice.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    // WebClient for making HTTP requests to the User Service
    private final WebClient userServiceWebClient;

    /**
     * Validates if a user exists with the given userId by making a request to the User Service.
     *
     * @param userId The unique identifier of the user to validate
     * @return Mono<Boolean> true if user exists, false otherwise
     * @throws RuntimeException with appropriate error message for different failure scenarios
     */
    public Mono<Boolean> validateUser(String userId) {
        log.debug("Initiating user validation for userId: {}", userId);
        
        return userServiceWebClient.get()
                .uri("/api/users/{userId}/validate", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(result -> 
                    log.debug("Successfully validated user with id: {}. Exists: {}", userId, result)
                )
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Error validating user {}: {}", userId, e.getResponseBodyAsString());
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new RuntimeException("User not found: " + userId));
                    } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid user ID format: " + userId));
                    }
                    return Mono.error(new RuntimeException("Failed to validate user: " + e.getMessage()));
                });
    }

    /**
     * Registers a new user in the system by sending the registration request to the User Service.
     *
     * @param request The registration request containing user details
     * @return Mono<UserResponse> containing the created user details
     * @throws RuntimeException with appropriate error messages for different failure scenarios
     */
    public Mono<UserResponse> registerUser(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());
        
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .doOnSuccess(response -> 
                    log.info("Successfully registered user with email: {}", request.getEmail())
                )
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Registration failed for email {}: {}", 
                            request.getEmail(), e.getResponseBodyAsString());
                            
                    if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("Invalid registration data: " + e.getMessage()));
                    } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        return Mono.error(new RuntimeException("User with this email already exists"));
                    } else if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return Mono.error(new RuntimeException("Registration service is currently unavailable"));
                    }
                    return Mono.error(new RuntimeException("Registration failed: " + e.getMessage()));
                });
    }
}