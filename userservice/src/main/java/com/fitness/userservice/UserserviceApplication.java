package com.fitness.userservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the User Service microservice.
 * 
 * <p>This service is responsible for managing user accounts, authentication, and profile information
 * in the Fitness Management System. It provides RESTful APIs for user registration, login,
 * profile management, and access control.</p>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>User registration and authentication</li>
 *   <li>JWT-based authentication</li>
 *   <li>User profile management</li>
 *   <li>Role-based access control</li>
 *   <li>Integration with other microservices via REST</li>
 *   <li>Service registration with Eureka</li>
 * </ul>
 * 
 * @author Fitness Application Team
 * @version 1.0
 */
@Slf4j
@SpringBootApplication
public class UserserviceApplication {

    /**
     * Main method to start the User Service application.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting User Service...");
        SpringApplication.run(UserserviceApplication.class, args);
        log.info("User Service started successfully and registered with Eureka");
    }
}
