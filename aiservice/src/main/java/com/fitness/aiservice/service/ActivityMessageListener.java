package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for listening to activity messages from RabbitMQ and processing them
 * to generate AI-powered fitness recommendations.
 * 
 * <p>This service acts as the entry point for processing fitness activities asynchronously.
 * It consumes messages from a configured RabbitMQ queue, processes them using the AI service,
 * and persists the generated recommendations to MongoDB.</p>
 * 
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Consume activity messages from RabbitMQ</li>
 *   <li>Coordinate the recommendation generation process</li>
 *   <li>Handle message processing failures with retry mechanism</li>
 *   <li>Persist recommendations to the database</li>
 * </ul>
 * 
 * @see ActivityAIService
 * @see RecommendationRepository
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    
    /** Service responsible for generating AI-powered recommendations */
    private final ActivityAIService aiService;
    
    /** Repository for persisting and retrieving recommendations */
    private final RecommendationRepository recommendationRepository;

    /**
     * The name of the RabbitMQ queue to listen to for activity messages.
     * Injected from application.yml configuration.
     */
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    /**
     * Listens to the configured RabbitMQ queue for activity messages and processes them
     * to generate fitness recommendations.
     * 
     * <p>This method is annotated with {@code @Retryable} to automatically retry failed
     * message processing up to 3 times with exponential backoff. If all retries fail,
     * the message will be sent to the dead-letter queue (if configured).</p>
     *
     * @param activity The deserialized Activity object from the message
     * @throws AmqpRejectAndDontRequeueException if the message processing fails after all retries
     */
    @Transactional
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    @RabbitListener(queues = "#{'${rabbitmq.queue.name}'}")
    public void processActivity(Activity activity) {
        try {
            log.info("Received activity message for processing: {}", activity.getId());
            
            // Generate AI-powered recommendation
            log.info("Generating recommendation for activity: {}", activity.getId());
            Recommendation recommendation = aiService.generateRecommendation(activity);
            log.debug("Generated recommendation: {}", recommendation);
            
            // Persist the recommendation
            log.info("Persisting recommendation to database...");
            Recommendation savedRecommendation = recommendationRepository.save(recommendation);
            log.info("Successfully saved recommendation with id: {}", savedRecommendation.getId());
            
        } catch (Exception e) {
            log.error("Error processing activity: {}", activity.getId(), e);
            
            // Re-throw as AmqpRejectAndDontRequeueException to prevent infinite retries
            // and allow dead-letter queue processing if configured
            throw new AmqpRejectAndDontRequeueException(
                "Failed to process activity: " + activity.getId(), e);
        }
    }
}
