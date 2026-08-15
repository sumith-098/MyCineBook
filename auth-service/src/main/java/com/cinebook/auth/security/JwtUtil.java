package com.cinebook.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Issues and validates JWTs for all three roles (CUSTOMER / OWNER / ADMIN).
 * The token carries: sub=user id, email, name, role — every other microservice
 * (booking-service, payment-service, admin-service, ...) validates the same
 * token using the same JWT_SECRET, so no session state is shared anywhere.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;
    private final String issuer;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiry-ms}") long accessExpiryMs,
            @Value("${app.jwt.refresh-token-expiry-ms}") long refreshExpiryMs,
            @Value("${app.jwt.issuer}") String issuer) {
        // secret must be >= 256 bits for HS256; enforce at startup rather than failing silently
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters (256 bits) long");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(Long userId, String email, String name, String role) {
        return buildToken(userId, email, name, role, accessExpiryMs, "access");
    }

    public String generateRefreshToken(Long userId, String email, String name, String role) {
        return buildToken(userId, email, name, role, refreshExpiryMs, "refresh");
    }

    private String buildToken(Long userId, String email, String name, String role, long expiryMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMs);
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claims(Map.of(
                        "email", email,
                        "name", name == null ? "" : name,
                        "role", role,
                        "type", type
                ))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Short-lived (15 min) single-purpose token proving OTP ownership for /reset-password, replacing Flask's session flag. */
    public String generatePasswordResetToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 15 * 60 * 1000L);
        return Jwts.builder()
                .issuer(issuer)
                .subject(email)
                .claims(Map.of("type", "password_reset"))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validatePasswordResetToken(String token, String expectedEmail) {
        Claims claims = parseClaims(token); // throws if invalid/expired
        if (!"password_reset".equals(claims.get("type", String.class)) || !claims.getSubject().equals(expectedEmail)) {
            throw new io.jsonwebtoken.JwtException("Invalid reset token");
        }
        return claims.getSubject();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessExpiryMs() { return accessExpiryMs; }
}
