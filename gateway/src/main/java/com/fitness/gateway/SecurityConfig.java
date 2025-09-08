package com.fitness.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.core.env.Environment;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri")
    public ReactiveJwtDecoder jwtDecoder() {
        String jwkSetUri = env.getRequiredProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri");
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Configure security
        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange();

        // Check if JWT is configured
        if (env.containsProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri")) {
            authorizeExchange.anyExchange().authenticated();
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        } else {
            // Allow all if JWT is not configured
            authorizeExchange.anyExchange().permitAll();
        }

        return authorizeExchange.and().build();
    }




}
