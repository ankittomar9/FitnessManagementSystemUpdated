package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        log.info("Calling User validation API for User with userID: {}", userId);
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to validate null or empty user ID");
            return false;
        }
        
        try {
            boolean isValid = userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            log.debug("Successfully validated user: {}. Result: {}", userId, isValid);
            return isValid;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found with ID: {}", userId);
                throw new RuntimeException("User not found: " + userId);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.error("Invalid request while validating user ID: {}", userId, e);
                throw new RuntimeException("Invalid Request: " + userId);
            } else {
                log.error("Unexpected error validating user ID: {}", userId, e);
            }
        } catch (Exception e) {
            log.error("Error while validating user ID: {}", userId, e);
        }
        log.warn("User validation failed for ID: {}", userId);
        return false;
    }

}
