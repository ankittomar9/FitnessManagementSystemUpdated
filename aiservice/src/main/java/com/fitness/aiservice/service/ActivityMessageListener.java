package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
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

    // Injects the queue name from application.yml
    @Value("${rabbitmq.queue.name}")
    private String queueName;

    /**
     * Listens to the queue specified in application.yml
     * @param activity The deserialized Activity object from the message
     */
    @RabbitListener(queues = "#{'${rabbitmq.queue.name}'}")
    public void processActivity(Activity activity) {
        log.info("Received activity message for processing: {}", activity.getId());
        // Add your message processing logic here
        log.info("Generated Recommendation : {} ",aiService.generateRecommendation(activity));
    }
}
