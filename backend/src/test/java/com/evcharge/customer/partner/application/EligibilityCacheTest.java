package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EligibilityCacheTest {

    private static final Instant NOW = Instant.parse("2026-11-22T14:00:00Z");

    private final AtomicReference<Instant> now = new AtomicReference<>(NOW);
    private final Clock clock = mock(Clock.class);
    private final EligibilityCache cache = new EligibilityCache(clock);
    private final UUID customer = UUID.randomUUID();
    private final AtomicInteger calls = new AtomicInteger();

    @BeforeEach
    void movableClock() {
        when(clock.instant()).thenAnswer(invocation -> now.get());
    }

    private Function<UUID, FreeChargingEligibility> answering(FreeChargingEligibility answer) {
        return id -> {
            calls.incrementAndGet();
            return answer;
        };
    }

    private static FreeChargingEligibility eligibleUntil(Instant end) {
        return new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(), LocalDate.of(2026, 11, 22), end);
    }

    private void advance(Duration step) {
        now.set(now.get().plus(step));
    }

    @Test
    void aSecondLookupWithinThirtySecondsReusesTheAnswer() {
        FreeChargingEligibility answer = eligibleUntil(NOW.plus(Duration.ofHours(2)));
        cache.get(customer, answering(answer));
        advance(Duration.ofSeconds(29));
        assertThat(cache.get(customer, answering(answer))).isEqualTo(answer);
        assertThat(calls).hasValue(1);
    }

    @Test
    void anAnswerExpiresAfterThirtySeconds() {
        FreeChargingEligibility answer = eligibleUntil(NOW.plus(Duration.ofHours(2)));
        cache.get(customer, answering(answer));
        advance(Duration.ofSeconds(30));
        cache.get(customer, answering(answer));
        assertThat(calls).hasValue(2);
    }

    @Test
    void anUnavailableAnswerIsKeptAtMostFiveSeconds() {
        cache.get(customer, answering(FreeChargingEligibility.unavailable()));
        advance(Duration.ofSeconds(4));
        cache.get(customer, answering(FreeChargingEligibility.unavailable()));
        assertThat(calls).hasValue(1);
        advance(Duration.ofSeconds(1));
        cache.get(customer, answering(FreeChargingEligibility.unavailable()));
        assertThat(calls).hasValue(2);
    }

    @Test
    void anEligibleAnswerIsNeverServedOnceItsWindowHasEnded() {
        cache.get(customer, answering(eligibleUntil(NOW.plus(Duration.ofSeconds(10)))));
        advance(Duration.ofSeconds(10));
        FreeChargingEligibility closed = new FreeChargingEligibility(false, "OUTSIDE_WINDOW", null, null, null);
        assertThat(cache.get(customer, answering(closed))).isEqualTo(closed);
        assertThat(calls).hasValue(2);
    }

    @Test
    void eachCustomerHasItsOwnEntry() {
        cache.get(customer, answering(FreeChargingEligibility.unavailable()));
        cache.get(UUID.randomUUID(), answering(FreeChargingEligibility.unavailable()));
        assertThat(calls).hasValue(2);
    }

    @Test
    void theCacheStaysBounded() {
        FreeChargingEligibility answer = eligibleUntil(NOW.plus(Duration.ofHours(2)));
        for (int i = 0; i <= EligibilityCache.MAX_ENTRIES; i++) {
            cache.get(UUID.randomUUID(), answering(answer));
        }
        assertThat(cache.size()).isLessThanOrEqualTo(EligibilityCache.MAX_ENTRIES);
    }
}
