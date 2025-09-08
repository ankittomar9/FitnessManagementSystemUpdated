package com.fitness.gateway;

import com.fitness.gateway.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeyCloakUserSyncFilter implements WebFilter {
    private final UserService userService;


    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String userId=exchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token=exchange.getRequest().getHeaders().getFirst("Authorization");

        if(userId!=null || token!=null){
            return userService.validateUser(userId)
            .flatMap(valid->{
                if(valid){
                    return chain.filter(exchange);
                }else{
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            });
        }
    }



}
