package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Clock;
import java.time.Duration;

@Configuration
@ConditionalOnProperty(prefix = "zella.rate-limit", name = "store", havingValue = "redis")
class RedisRateLimitConfiguration {

    private static final Duration REDIS_COOL_DOWN = Duration.ofSeconds(10);

    @Bean
    @Primary
    RateLimiter sharedRateLimiter(StringRedisTemplate redis, InMemoryRateLimiter local, Clock clock) {
        return new FailoverRateLimiter(new RedisRateLimiter(redis), local, clock, REDIS_COOL_DOWN);
    }
}
