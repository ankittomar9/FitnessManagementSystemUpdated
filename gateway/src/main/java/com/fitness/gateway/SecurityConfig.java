package com.fitness.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration class for the API Gateway.
 * Configures security rules, CORS, and JWT authentication.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // CORS configuration
    private static final List<String> ALLOWED_ORIGINS = List.of("http://localhost:5173");
    private static final List<String> ALLOWED_METHODS = 
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private static final List<String> ALLOWED_HEADERS = 
        Arrays.asList("Authorization", "Content-Type", "X-User-ID");
    private static final String API_PATH_PATTERN = "/api/**";

    /**
     * Configures the security filter chain for the application.
     * 
     * @param http The ServerHttpSecurity to configure
     * @return The configured SecurityWebFilterChain
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        log.info("Configuring security filter chain");
        
        return http
                // Disable CSRF as we're using JWT for stateless authentication
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Configure authorization rules
                .authorizeExchange(exchange -> {
                    // Public endpoints (example: uncomment and modify as needed)
                    // exchange.pathMatchers("/actuator/health").permitAll();
                    
                    // All other requests require authentication
                    exchange.anyExchange().authenticated();
                    
                    log.debug("Configured authorization rules - all endpoints require authentication");
                })
                
                // Configure OAuth2 Resource Server with JWT
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(Customizer.withDefaults());
                    log.debug("Configured OAuth2 Resource Server with JWT");
                })
                
                .build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * 
     * @return Configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS with allowed origins: {}", ALLOWED_ORIGINS);
        
        CorsConfiguration config = new CorsConfiguration();
        
        // Configure allowed origins
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        
        // Configure allowed HTTP methods
        config.setAllowedMethods(ALLOWED_METHODS);
        log.debug("Configured allowed methods: {}", ALLOWED_METHODS);
        
        // Configure allowed headers
        config.setAllowedHeaders(ALLOWED_HEADERS);
        log.debug("Configured allowed headers: {}", ALLOWED_HEADERS);
        
        // Allow credentials (cookies, authorization headers) to be exposed
        config.setAllowCredentials(true);
        
        // Set the max age of the CORS pre-flight request
        config.setMaxAge(3600L);
        
        // Configure CORS for all endpoints under /api/**
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(API_PATH_PATTERN, config);
        
        log.debug("CORS configuration completed for path pattern: {}", API_PATH_PATTERN);
        return source;
    }
}