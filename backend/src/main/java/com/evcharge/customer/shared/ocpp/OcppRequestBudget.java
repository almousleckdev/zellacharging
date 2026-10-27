package com.evcharge.customer.shared.ocpp;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

class OcppRequestBudget {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final int perMinute;
    private final int priorityReserve;
    private final Clock clock;
    private final Counter throttled;
    private Instant windowStart = Instant.MIN;
    private int used;
    private Instant blockedUntil = Instant.MIN;

    OcppRequestBudget(int perMinute, int priorityReserve, Clock clock, MeterRegistry registry) {
        this.perMinute = perMinute;
        this.priorityReserve = priorityReserve;
        this.clock = clock;
        this.throttled = registry.counter("zella.ocpp.polls.throttled");
    }

    synchronized boolean allow(boolean background) {
        Instant now = clock.instant();
        roll(now);
        int ceiling = background ? perMinute - priorityReserve : perMinute;
        boolean allowed = now.isAfter(blockedUntil) && used < ceiling;
        if (allowed) {
            used++;
        } else {
            throttled.increment();
        }
        return allowed;
    }

    synchronized void recordPriority() {
        roll(clock.instant());
        used++;
    }

    synchronized void backOff(Duration duration) {
        blockedUntil = clock.instant().plus(duration);
    }

    private void roll(Instant now) {
        if (windowStart.plus(WINDOW).isBefore(now)) {
            windowStart = now;
            used = 0;
        }
    }
}
