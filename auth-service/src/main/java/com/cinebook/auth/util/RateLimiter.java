package com.cinebook.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory sliding-window rate limiter — direct port of the Flask app's `rate_limit()`.
 * Fine for a single instance; if auth-service is ever scaled horizontally, swap this
 * for a Redis-backed counter instead.
 */
@Component
public class RateLimiter {

    private final Map<String, List<Instant>> attempts = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.max-attempts:10}")
    private int defaultMaxAttempts;

    @Value("${app.rate-limit.window-minutes:5}")
    private int defaultWindowMinutes;

    public boolean isBlocked(String key) {
        return isBlocked(key, defaultMaxAttempts, defaultWindowMinutes);
    }

    public boolean isBlocked(String key, int maxAttempts, int windowMinutes) {
        Instant now = Instant.now();
        List<Instant> bucket = attempts.computeIfAbsent(key, k -> new java.util.concurrent.CopyOnWriteArrayList<>());
        bucket.removeIf(t -> t.isBefore(now.minusSeconds(windowMinutes * 60L)));
        if (bucket.size() >= maxAttempts) {
            return true;
        }
        bucket.add(now);
        return false;
    }
}
