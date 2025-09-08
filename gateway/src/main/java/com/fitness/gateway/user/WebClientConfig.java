package com.fitness.gateway.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Configuration class for WebClient instances used for service-to-service communication.
 * Provides pre-configured WebClient beans with load balancing and request/response logging.
 */
@Slf4j
@Configuration
public class WebClientConfig {
    
    // Service ID as registered in Eureka
    private static final String USER_SERVICE_ID = "USER-SERVICE";
    
    /**
     * Creates a load-balanced WebClient.Builder with default configurations.
     * This builder is used to create WebClient instances for service-to-service communication.
     *
     * @return Configured WebClient.Builder instance with load balancing
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        log.debug("Creating load-balanced WebClient.Builder");
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse());
    }

    /**
     * Creates a WebClient specifically configured for the User Service.
     * 
     * @param webClientBuilder The load-balanced WebClient.Builder
     * @return Configured WebClient for User Service communication
     */
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder webClientBuilder) {
        log.info("Configuring WebClient for User Service with service ID: {}", USER_SERVICE_ID);
        return webClientBuilder
                .baseUrl("http://" + USER_SERVICE_ID)
                .build();
    }
    
    /**
     * Logs outgoing requests for debugging purposes.
     * 
     * @return ExchangeFilterFunction that logs request details
     */
    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.debug("Outgoing {} request to {}", 
                    clientRequest.method(), 
                    clientRequest.url());
            return next.exchange(clientRequest);
        };
    }
    
    /**
     * Logs incoming responses for debugging purposes.
     * 
     * @return ExchangeFilterFunction that logs response status
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Received response with status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
