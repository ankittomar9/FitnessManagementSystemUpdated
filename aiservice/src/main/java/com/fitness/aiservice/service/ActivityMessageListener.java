package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {
    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    // Injects the queue name from application.yml
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    /**
     * Listens to the queue specified in application.yml
     * @param activity The deserialized Activity object from the message
     */
    @RabbitListener(queues = "#{'${rabbitmq.queue.name}'}")
    public void processActivity(Activity activity) {
        try {
            log.info("Received activity message for processing: {}", activity.getId());
            
            // Generate recommendation
            log.info("Generating recommendation for activity: {}", activity.getId());
            Recommendation recommendation = aiService.generateRecommendation(activity);
            log.debug("Generated recommendation: {}", recommendation);
            
            // Save recommendation
            log.info("Saving recommendation to database...");
            Recommendation savedRecommendation = recommendationRepository.save(recommendation);
            log.info("Successfully saved recommendation with id: {}", savedRecommendation.getId());
            
        } catch (Exception e) {
            log.error("Error processing activity: " + activity.getId(), e);
            // You might want to implement retry logic or dead-letter queue handling here
            throw e; // Re-throw to trigger RabbitMQ's retry mechanism if configured
        }
    }
}
