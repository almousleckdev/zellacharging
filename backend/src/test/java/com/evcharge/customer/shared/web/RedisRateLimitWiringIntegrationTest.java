package com.evcharge.customer.shared.web;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "zella.rate-limit.store=redis")
class RedisRateLimitWiringIntegrationTest extends PostgresTestBase {

    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void redis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url", () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
    }

    @Autowired
    RateLimiter limiter;

    @Autowired
    StringRedisTemplate redis;

    @Test
    void theApplicationCountsInRedisWhenTheRedisStoreIsSelected() {
        String bucket = "wiring-" + UUID.randomUUID();

        assertThat(limiter).isInstanceOf(FailoverRateLimiter.class);
        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, Duration.ofMinutes(1))).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, Duration.ofMinutes(1))).isFalse();
        assertThat(redis.hasKey("rl:" + bucket + "|ip:1")).isTrue();
    }
}
