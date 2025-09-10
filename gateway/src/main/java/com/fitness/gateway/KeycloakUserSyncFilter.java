package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFilter implementation that synchronizes Keycloak users with the application's user database.
 * This filter checks if an authenticated user exists in the local database and creates a new user
 * if they don't exist, using information from the JWT token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String DUMMY_PASSWORD = "dummy@123123";
    
    private final UserService userService;

    /**
     * Processes the web request to synchronize the authenticated user with the local database.
     * 
     * @param exchange The current server exchange
     * @param chain Provides a way to delegate to the next filter
     * @return A Mono that completes when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("Processing request for user synchronization");
        
        String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        
        if (token == null) {
            log.debug("No authorization token found, skipping user synchronization");
            return chain.filter(exchange);
        }
        
        RegisterRequest registerRequest = getUserDetails(token);
        
        if (userId == null && registerRequest != null) {
            userId = registerRequest.getKeycloakId();
            log.debug("Extracted userId from token: {}", userId);
        }

        if (userId != null && token != null) {
            // Create final copies of variables to be used in lambda expressions
            final String finalUserId = userId;
            final RegisterRequest finalRegisterRequest = registerRequest;
            
            log.debug("Initiating user validation for userId: {}", userId);
            
            return userService.validateUser(userId)
                    .flatMap(exists -> {
                        if (!exists) {
                            log.info("User not found in local database. Attempting to register new user: {}", finalUserId);
                            
                            if (finalRegisterRequest != null) {
                                return userService.registerUser(finalRegisterRequest)
                                        .doOnSuccess(response -> 
                                            log.info("Successfully registered user: {}", finalUserId)
                                        )
                                        .then(Mono.empty());
                            } else {
                                log.warn("Cannot register user: No registration details available for userId: {}", finalUserId);
                                return Mono.empty();
                            }
                        } else {
                            log.debug("User already exists in local database. Skipping registration for userId: {}", finalUserId);
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        log.debug("Adding X-User-ID header to request: {}", finalUserId);
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header(USER_ID_HEADER, finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }
        
        log.debug("Insufficient information for user synchronization. Proceeding without user sync.");
        return chain.filter(exchange);
    }

    /**
     * Extracts user details from a JWT token.
     * 
     * @param token The JWT token string (with or without 'Bearer ' prefix)
     * @return RegisterRequest containing user details, or null if parsing fails
     */
    private RegisterRequest getUserDetails(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Cannot extract user details: Token is null or empty");
            return null;
        }
        
        try {
            log.debug("Extracting user details from JWT token");
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloakId(claims.getStringClaim("sub"));
            registerRequest.setPassword(DUMMY_PASSWORD); // Using a dummy password as it's not needed for OAuth2
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));
            
            log.debug("Successfully extracted user details for: {}", registerRequest.getEmail());
            return registerRequest;
            
        } catch (Exception e) {
            log.error("Failed to parse JWT token or extract user details: {}", e.getMessage(), e);
            return null;
        }
    }
}