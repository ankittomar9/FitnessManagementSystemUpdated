package com.fitness.aiservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service responsible for generating AI-powered fitness recommendations based on user activities.
 * 
 * <p>This service acts as a bridge between the application and the Gemini AI service,
 * processing activity data and transforming AI responses into structured recommendations.</p>
 * 
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Generate prompts for the AI based on activity data</li>
 *   <li>Process AI responses and extract structured recommendations</li>
 *   <li>Handle errors and retries for AI service calls</li>
 *   <li>Transform raw AI responses into application domain objects</li>
 * </ul>
 * 
 * @see GeminiService
 * @see Recommendation
 * @see Activity
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    
    /** Service for interacting with the Gemini AI API */
    private final GeminiService geminiService;
    
    /** JSON object mapper for parsing AI responses */
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Maximum number of retries for AI service calls
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Generates a personalized fitness recommendation based on the provided activity data.
     * 
     * <p>This method orchestrates the recommendation generation process by:
     * 1. Creating a detailed prompt for the AI
     * 2. Sending the prompt to the Gemini AI service
     * 3. Processing and validating the AI response
     * 4. Converting the response into a structured Recommendation object</p>
     * 
     * @param activity The activity data to generate recommendations for
     * @return A Recommendation object containing AI-generated fitness advice
     * @throws IllegalStateException if the AI service returns an invalid or empty response
     */
    @Retryable(
        value = {Exception.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = RETRY_DELAY_MS, multiplier = 2.0)
    )
    public Recommendation generateRecommendation(Activity activity) {
        Objects.requireNonNull(activity, "Activity cannot be null");
        log.debug("Generating recommendation for activity: {}", activity.getId());
        
        try {
            String prompt = createPromptForActivity(activity);
            log.trace("Generated AI prompt for activity {}: {}", activity.getId(), prompt);
            
            String aiResponse = geminiService.getAnswer(prompt);
            log.debug("Received AI response for activity: {}", activity.getId());
            
            return processAiResponse(activity, aiResponse);
            
        } catch (Exception e) {
            log.error("Failed to generate recommendation for activity: " + activity.getId(), e);
            throw new IllegalStateException("Failed to generate AI recommendation", e);
        }
    }

    /**
     * Processes the raw AI response and converts it into a structured Recommendation object.
     * 
     * <p>This method handles the parsing and validation of the AI response, extracting
     * the relevant information and mapping it to the Recommendation model.</p>
     * 
     * @param activity The original activity that the recommendation is for
     * @param aiResponse The raw JSON response from the AI service
     * @return A populated Recommendation object
     * @throws JsonProcessingException if the AI response cannot be parsed
     * @throws IllegalStateException if the response is missing required fields
     */
    private Recommendation processAiResponse(Activity activity, String aiResponse) 
            throws JsonProcessingException {
        
        if (!StringUtils.hasText(aiResponse)) {
            throw new IllegalStateException("Empty response received from AI service");
        }

        log.debug("Processing AI response for activity: {}", activity.getId());
        
        try {
            // Parse the outer JSON response
            JsonNode rootNode = objectMapper.readTree(aiResponse);
            
            // Navigate to the text content containing the JSON response
            String jsonContent = rootNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText()
                .replaceAll("```json\\n|", "")
                .replaceAll("\\n```", "")
                .trim();

            log.trace("Extracted JSON content from AI response: {}", jsonContent);
            
            // Parse the inner JSON content
            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafetyGuidelines(analysisJson.path("safety"));

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType().name())// Here as well
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType().name()) // Check needed here
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(item -> safety.add(item.asText()));
        }
        return safety.isEmpty() ?
                Collections.singletonList("Follow general safety guidelines") :
                safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestion -> {
                String workout = suggestion.path("workout").asText();
                String description = suggestion.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ?
                Collections.singletonList("No specific suggestions provided") :
                suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));
            });
        }
        return improvements.isEmpty() ?
                Collections.singletonList("No specific improvements provided") :
                improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
        {
          "analysis": {
            "overall": "Overall analysis here",
            "pace": "Pace analysis here",
            "heartRate": "Heart rate analysis here",
            "caloriesBurned": "Calories analysis here"
          },
          "improvements": [
            {
              "area": "Area name",
              "recommendation": "Detailed recommendation"
            }
          ],
          "suggestions": [
            {
              "workout": "Workout name",
              "description": "Detailed workout description"
            }
          ],
          "safety": [
            "Safety point 1",
            "Safety point 2"
          ]
        }

        Analyze this activity:
        Activity Type: %s
        Duration: %d minutes
        Calories Burned: %d
        Additional Metrics: %s
        
        Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines.
        Ensure the response follows the EXACT JSON format shown above.
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}