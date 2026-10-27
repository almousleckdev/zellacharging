package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.RateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FailoverRateLimiterTest {

    private static final Duration COOL_DOWN = Duration.ofSeconds(10);
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final AtomicBoolean redisDown = new AtomicBoolean(false);
    private final AtomicInteger primaryCalls = new AtomicInteger();
    private final AtomicInteger fallbackCalls = new AtomicInteger();
    private final SettableClock clock = new SettableClock();

    private final RateLimiter primary = (bucket, key, limit, window) -> {
        primaryCalls.incrementAndGet();
        if (redisDown.get()) {
            throw new IllegalStateException("redis is down");
        }
        return true;
    };

    private final RateLimiter fallback = (bucket, key, limit, window) -> {
        fallbackCalls.incrementAndGet();
        return false;
    };

    private final FailoverRateLimiter limiter = new FailoverRateLimiter(primary, fallback, clock, COOL_DOWN);

    private boolean acquire() {
        return limiter.tryAcquire("login", "ip:1", 5, WINDOW);
    }

    @Test
    void theSharedLimiterIsUsedWhileItIsHealthy() {
        assertThat(acquire()).isTrue();

        assertThat(primaryCalls.get()).isOne();
        assertThat(fallbackCalls.get()).isZero();
    }

    @Test
    void theLocalLimiterAnswersWhenTheSharedOneFails() {
        redisDown.set(true);

        assertThat(acquire()).isFalse();

        assertThat(fallbackCalls.get()).isOne();
    }

    @Test
    void theSharedLimiterIsLeftAloneDuringTheCoolDown() {
        redisDown.set(true);
        acquire();
        clock.advance(Duration.ofSeconds(5));

        acquire();
        acquire();

        assertThat(primaryCalls.get()).isOne();
        assertThat(fallbackCalls.get()).isEqualTo(3);
    }

    @Test
    void theSharedLimiterIsTriedAgainAfterTheCoolDownAndRecovers() {
        redisDown.set(true);
        acquire();
        redisDown.set(false);
        clock.advance(COOL_DOWN.plusSeconds(1));

        assertThat(acquire()).isTrue();

        assertThat(primaryCalls.get()).isEqualTo(2);
        assertThat(fallbackCalls.get()).isOne();
    }

    @Test
    void aStillFailingSharedLimiterStartsANewCoolDown() {
        redisDown.set(true);
        acquire();
        clock.advance(COOL_DOWN.plusSeconds(1));
        acquire();

        acquire();

        assertThat(primaryCalls.get()).isEqualTo(2);
    }

    private static final class SettableClock extends Clock {

        private Instant now = Instant.parse("2026-10-27T10:00:00Z");

        void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
