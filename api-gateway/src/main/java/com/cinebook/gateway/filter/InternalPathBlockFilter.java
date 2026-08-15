package com.cinebook.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Service-to-service-only paths (booking-service's /internal/**, guarded there by
 * X-Internal-Api-Key) must never be reachable from the public internet through this gateway —
 * even though those endpoints already reject anything without the internal key, blocking the
 * path here entirely removes it from the public attack surface rather than relying on that
 * single layer of defense. Runs before routing.
 */
@Component
public class InternalPathBlockFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.contains("/internal/")) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200; // before JwtValidationFilter and before routing
    }
}
