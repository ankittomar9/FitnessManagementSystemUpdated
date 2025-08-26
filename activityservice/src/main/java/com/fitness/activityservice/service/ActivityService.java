package com.fitness.activityservice.service;

import com.fitness.activityservice.repository.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for activity-related business logic.
 * Handles activity tracking and retrieval operations.
 * Converts between entity and DTO objects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;

    public ActivityResponse trackActivity(ActivityRequest request) {
        log.info("Received request to track activity for user: {}", request.getUserId());
        
        try {
            log.debug("Validating user: {}", request.getUserId());
            boolean isValidUser = userValidationService.validateUser(request.getUserId());
            
            if (!isValidUser) {
                log.warn("Invalid user ID provided: {}", request.getUserId());
                throw new RuntimeException("User not found with id: " + request.getUserId());
            }
            
            log.debug("User validation successful for ID: {}", request.getUserId());

            log.debug("Creating activity record for user: {}", request.getUserId());
            Activity activity = Activity.builder()
                    .userId(request.getUserId())
                    .type(request.getType())
                    .duration(request.getDuration())
                    .caloriesBurned(request.getCaloriesBurned())
                    .startTime(request.getStartTime())
                    .additionalMetrics(request.getAdditionalMetrics())
                    .build();

            log.debug("Saving activity to repository: {}", activity);
            Activity savedActivity = activityRepository.save(activity);
            log.info("Successfully saved activity with ID: {} for user: {}", 
                    savedActivity.getId(), savedActivity.getUserId());
                    
            return mapToResponse(savedActivity);
        } catch (Exception e) {
            log.error("Error tracking activity for user: {}", request.getUserId(), e);
            throw new RuntimeException("Failed to track activity: " + e.getMessage(), e);
        }
    }
    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponse> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    public ActivityResponse getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + activityId ));

    }
}
