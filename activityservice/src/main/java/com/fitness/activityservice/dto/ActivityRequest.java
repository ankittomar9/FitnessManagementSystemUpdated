package com.fitness.activityservice.dto;

import com.fitness.activityservice.model.ActivityType;

import java.time.LocalDateTime;
import java.util.Map;

public class ActivityRequest {
    private String userId;
    private ActivityType type;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;

}
