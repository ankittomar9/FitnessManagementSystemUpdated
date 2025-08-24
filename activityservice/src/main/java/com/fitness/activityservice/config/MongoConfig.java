package com.fitness.activityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuration class for MongoDB.
 * Enables MongoDB auditing for automatic timestamp management.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
