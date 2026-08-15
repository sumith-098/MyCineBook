package com.cinebook.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Fail-fast JWT check at the edge. IMPORTANT — this is a narrower job than full authorization:
 *  - If no Authorization header is present at all, the request is passed through unchanged.
 *    Plenty of routes are genuinely public (browsing movies/theaters, login, register, etc.) and
 *    the gateway doesn't try to know which — that list already exists, correctly, in each
 *    service's own SecurityConfig, and duplicating it here would just create a second copy that
 *    can drift out of sync and quietly become wrong.
 *  - If an Authorization header IS present, it must be a well-formed, correctly-signed,
 *    non-expired ACCESS token (not a refresh token) — garbage or expired tokens are rejected
 *    here with a clean 401 instead of being forwarded to a backend at all.
 *  - Role/ownership authorization (is this user allowed to do THIS) is still enforced entirely
 *    by the destination service, same as always. This filter only ever narrows what reaches
 *    them, never widens it.
 */
@Component
public class JwtValidationFilter implements GlobalFilter, Ordered {

    private final SecretKey signingKey;

    public JwtValidationFilter(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters (256 bits) long");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange); // no token offered — let the destination service decide if one's required
        }

        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(header.substring(7)).getPayload();
            if (!"access".equals(claims.get("type", String.class))) {
                return reject(exchange, "Refresh tokens cannot be used to call the API.");
            }
        } catch (Exception e) {
            return reject(exchange, "Invalid or expired session — please log in again.");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"success\":false,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        var buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // after InternalPathBlockFilter, before routing
    }
}
