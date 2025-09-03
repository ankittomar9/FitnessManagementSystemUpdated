package com.fitness.aiservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the AI Service microservice.
 * 
 * <p>This service is responsible for processing fitness activities and generating
 * personalized recommendations using AI. It listens to activity messages from a message queue,
 * processes them using Gemini AI, and stores the generated recommendations in MongoDB.</p>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Listens to activity messages from RabbitMQ</li>
 *   <li>Processes activities using Gemini AI</li>
 *   <li>Stores recommendations in MongoDB</li>
 *   <li>Provides REST APIs to retrieve recommendations</li>
 *   <li>Registers with Eureka Service Discovery</li>
 * </ul>
 * 
 * @author Fitness Application Team
 * @version 1.0
 */
@Slf4j
@SpringBootApplication
public class AiserviceApplication {

    /**
     * Main method to start the AI Service application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting AI Service...");
        SpringApplication.run(AiserviceApplication.class, args);
        log.info("AI Service started successfully and registered with Eureka");
    }
}
