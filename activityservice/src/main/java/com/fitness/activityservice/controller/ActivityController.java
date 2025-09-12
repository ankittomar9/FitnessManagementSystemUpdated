package com.fitness.activityservice.controller;

import com.fitness.activityservice.dto.ActivityRequest;
import com.fitness.activityservice.dto.ActivityResponse;
import com.fitness.activityservice.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for activity-related endpoints.
 * Handles tracking new activities and retrieving activity data.
 * Requires user authentication via X-USER-ID header.
 */
@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityController {

        private ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponse> trackActivity(@RequestBody ActivityRequest request ,@RequestHeader ("X-USER-ID") String userId) {
        if(userId != null){
            request.setUserId(userId);
        }
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>>getUserActivities(@RequestHeader ("X-USER-ID") String userId) {
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }


    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponse>getActivity(@PathVariable  String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }


}

/*
* sequenceDiagram
    participant Client
    participant ActivityService
    participant UserValidationService
    participant UserService

    Client->>ActivityService: POST /api/activities (trackActivity)
    ActivityService->>UserValidationService: validateUser(userId)
    UserValidationService->>UserService: GET /api/users/{userId}/validate
    UserService-->>UserValidationService: Boolean response (true/false)
    alt User not found
        UserValidationService-->>ActivityService: false or throws exception
        ActivityService-->>Client: 404 Not Found
    else User valid
        ActivityService->>ActivityRepository: save(activity)
        ActivityRepository-->>ActivityService: Saved activity
        ActivityService-->>Client: 201 Created with activity details
    end
* */

