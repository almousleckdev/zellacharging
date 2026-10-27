package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.RateLimiter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class InMemoryRateLimiter implements RateLimiter {

    private record Window(long endsAtMillis, int used) {
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final Clock clock;

    InMemoryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean tryAcquire(String bucket, String key, int limit, Duration window) {
        long now = clock.millis();
        boolean[] allowed = {false};
        windows.compute(bucket + "|" + key, (name, current) -> {
            Window active = current == null || current.endsAtMillis() <= now
                    ? new Window(now + window.toMillis(), 0) : current;
            allowed[0] = active.used() < limit;
            return allowed[0] ? new Window(active.endsAtMillis(), active.used() + 1) : active;
        });
        return allowed[0];
    }

    @Scheduled(fixedDelay = 60_000)
    void evictExpired() {
        long now = clock.millis();
        windows.values().removeIf(window -> window.endsAtMillis() <= now);
    }
}
