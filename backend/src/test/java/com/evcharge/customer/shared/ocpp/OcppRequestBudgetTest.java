package com.evcharge.customer.shared.ocpp;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class OcppRequestBudgetTest {

    private static final Instant START = Instant.parse("2026-10-16T12:00:00Z");

    private static final class MovableClock extends Clock {
        private Instant now = START;

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MovableClock clock = new MovableClock();
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OcppRequestBudget budget = new OcppRequestBudget(20, 5, clock, registry);

    @Test
    void pollingStopsBeforeTheReserveSoStopCommandsAlwaysHaveRoom() {
        int allowed = 0;
        for (int call = 0; call < 40; call++) {
            allowed += budget.allow(true) ? 1 : 0;
        }
        assertThat(allowed).isEqualTo(15);
        assertThat(registry.counter("zella.ocpp.polls.throttled").count()).isEqualTo(25.0);
    }

    @Test
    void interactiveReadsMayUseTheReserveThatBackgroundPollingCannot() {
        for (int call = 0; call < 15; call++) {
            budget.allow(true);
        }
        assertThat(budget.allow(true)).isFalse();
        assertThat(budget.allow(false)).isTrue();
    }

    @Test
    void commandsCountAgainstTheBudgetButAreNeverRefused() {
        for (int call = 0; call < 30; call++) {
            budget.recordPriority();
        }
        assertThat(budget.allow(true)).isFalse();
    }

    @Test
    void theBudgetRenewsEveryMinute() {
        for (int call = 0; call < 20; call++) {
            budget.allow(true);
        }
        assertThat(budget.allow(true)).isFalse();
        clock.advance(Duration.ofSeconds(61));
        assertThat(budget.allow(true)).isTrue();
    }

    @Test
    void aTooManyRequestsAnswerPausesPollingForTheRequestedTime() {
        budget.backOff(Duration.ofSeconds(30));
        assertThat(budget.allow(true)).isFalse();
        clock.advance(Duration.ofSeconds(31));
        assertThat(budget.allow(true)).isTrue();
    }
}
