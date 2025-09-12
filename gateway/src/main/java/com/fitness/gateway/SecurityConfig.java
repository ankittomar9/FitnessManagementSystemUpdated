package com.fitness.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
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
    
    @Value("${spring.main.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    /**
     * Configures the security filter chain for the application.
     * 
     * @param http The ServerHttpSecurity to configure
     * @return The configured SecurityWebFilterChain
     */
    /**
     * Configures the ReactiveJwtDecoder for JWT validation.
     * Uses the JWK Set URI to fetch public keys for token verification.
     * 
     * @return Configured ReactiveJwtDecoder instance
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        log.info("Configuring ReactiveJwtDecoder with JWK Set URI: {}", jwkSetUri);
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

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
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                )
                
                .build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * 
     * @return Configured CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-ID"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}