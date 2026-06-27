package com.logicore.security.application.usecase;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bucket4j-based in-memory rate limiter keyed by client identifier (IP or API key prefix).
 * Default: 100 requests per minute per client.
 */
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String clientKey) {
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> newBucket());
        return bucket.tryConsume(1);
    }

    public boolean tryConsume(String clientKey, long tokens) {
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> newBucket());
        return bucket.tryConsume(tokens);
    }

    public long availableTokens(String clientKey) {
        Bucket bucket = buckets.computeIfAbsent(clientKey, k -> newBucket());
        return bucket.getAvailableTokens();
    }

    private static Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
