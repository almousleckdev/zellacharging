package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
class EligibilityCache {

    static final int MAX_ENTRIES = 10_000;

    private static final Duration ANSWER_LIFETIME = Duration.ofSeconds(30);
    private static final Duration FAILURE_LIFETIME = Duration.ofSeconds(5);

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    private final Clock clock;

    EligibilityCache(Clock clock) {
        this.clock = clock;
    }

    FreeChargingEligibility get(UUID customerId, Function<UUID, FreeChargingEligibility> source) {
        Instant now = clock.instant();
        Entry entry = entries.get(customerId);
        if (entry != null && entry.freshAt(now)) {
            return entry.answer();
        }
        FreeChargingEligibility answer = source.apply(customerId);
        store(customerId, answer, now);
        return answer;
    }

    int size() {
        return entries.size();
    }

    private void store(UUID customerId, FreeChargingEligibility answer, Instant now) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.values().removeIf(entry -> !entry.freshAt(now));
            if (entries.size() >= MAX_ENTRIES) {
                entries.clear();
            }
        }
        entries.put(customerId, new Entry(answer, expiry(answer, now)));
    }

    private static Instant expiry(FreeChargingEligibility answer, Instant now) {
        boolean failed = FreeChargingEligibility.UNAVAILABLE.equals(answer.reason());
        Instant until = now.plus(failed ? FAILURE_LIFETIME : ANSWER_LIFETIME);
        Instant windowEnd = answer.windowEndsAt();
        return answer.eligible() && windowEnd != null && windowEnd.isBefore(until) ? windowEnd : until;
    }

    private record Entry(FreeChargingEligibility answer, Instant expiresAt) {

        boolean freshAt(Instant now) {
            return now.isBefore(expiresAt);
        }
    }
}
