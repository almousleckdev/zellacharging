package com.evcharge.customer.shared.web;

import com.evcharge.customer.shared.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

class RedisRateLimiter implements RateLimiter {

    private static final String KEY_PREFIX = "rl:";
    private static final RedisScript<Long> COUNT_HIT = RedisScript.of("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return count""", Long.class);

    private final StringRedisTemplate redis;

    RedisRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean tryAcquire(String bucket, String key, int limit, Duration window) {
        Long count = redis.execute(COUNT_HIT, List.of(KEY_PREFIX + bucket + "|" + key),
                Long.toString(window.toMillis()));
        if (count == null) {
            throw new IllegalStateException("Redis returned no count for the rate limit script");
        }
        return count <= limit;
    }
}
