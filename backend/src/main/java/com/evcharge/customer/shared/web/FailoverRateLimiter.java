package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

class FailoverRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(FailoverRateLimiter.class);

    private final RateLimiter primary;
    private final RateLimiter fallback;
    private final Clock clock;
    private final Duration coolDown;
    private final AtomicReference<Instant> primaryRetryAt = new AtomicReference<>(Instant.MIN);

    FailoverRateLimiter(RateLimiter primary, RateLimiter fallback, Clock clock, Duration coolDown) {
        this.primary = primary;
        this.fallback = fallback;
        this.clock = clock;
        this.coolDown = coolDown;
    }

    @Override
    public boolean tryAcquire(String bucket, String key, int limit, Duration window) {
        if (!clock.instant().isBefore(primaryRetryAt.get())) {
            try {
                return primary.tryAcquire(bucket, key, limit, window);
            } catch (RuntimeException e) {
                primaryRetryAt.set(clock.instant().plus(coolDown));
                log.warn("Shared rate limiter unavailable, using the local one for {}: {}", coolDown, e.getMessage());
            }
        }
        return fallback.tryAcquire(bucket, key, limit, window);
    }
}
