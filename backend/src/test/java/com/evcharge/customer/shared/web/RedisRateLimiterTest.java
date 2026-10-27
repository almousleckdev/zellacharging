package com.evcharge.customer.shared.web;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisRateLimiterTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static LettuceConnectionFactory factory;
    private static StringRedisTemplate redis;

    @BeforeAll
    static void connect() {
        REDIS.start();
        factory = connectionTo(REDIS.getHost(), REDIS.getMappedPort(6379));
        redis = new StringRedisTemplate(factory);
    }

    @AfterAll
    static void disconnect() {
        factory.destroy();
        REDIS.stop();
    }

    private static LettuceConnectionFactory connectionTo(String host, int port) {
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(500)).build();
        LettuceConnectionFactory connection = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(host, port), client);
        connection.afterPropertiesSet();
        return connection;
    }

    private static String bucket() {
        return "bucket-" + UUID.randomUUID();
    }

    @Test
    void allowsUpToTheLimitThenRefuses() {
        RedisRateLimiter limiter = new RedisRateLimiter(redis);
        String bucket = bucket();

        assertThat(limiter.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 3, MINUTE)).isFalse();
    }

    @Test
    void keysAndBucketsAreCountedSeparately() {
        RedisRateLimiter limiter = new RedisRateLimiter(redis);
        String bucket = bucket();

        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:2", 1, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket(), "ip:1", 1, MINUTE)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, MINUTE)).isFalse();
    }

    @Test
    void theWindowReopensOnceItHasExpired() throws Exception {
        RedisRateLimiter limiter = new RedisRateLimiter(redis);
        String bucket = bucket();
        Duration shortWindow = Duration.ofMillis(300);

        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, shortWindow)).isTrue();
        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, shortWindow)).isFalse();
        Thread.sleep(450);

        assertThat(limiter.tryAcquire(bucket, "ip:1", 1, shortWindow)).isTrue();
    }

    @Test
    void everyCounterExpiresSoRedisNeverFillsUp() {
        RedisRateLimiter limiter = new RedisRateLimiter(redis);
        String bucket = bucket();
        limiter.tryAcquire(bucket, "ip:1", 5, MINUTE);

        Long ttl = redis.getExpire("rl:" + bucket + "|ip:1");

        assertThat(ttl).isBetween(1L, 60L);
    }

    @Test
    void twoReplicasShareOneCounter() {
        RedisRateLimiter first = new RedisRateLimiter(redis);
        RedisRateLimiter second = new RedisRateLimiter(new StringRedisTemplate(factory));
        String bucket = bucket();

        assertThat(first.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(second.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(first.tryAcquire(bucket, "ip:1", 3, MINUTE)).isTrue();
        assertThat(second.tryAcquire(bucket, "ip:1", 3, MINUTE)).isFalse();
    }

    @Test
    void parallelCallsNeverExceedTheLimit() throws Exception {
        RedisRateLimiter limiter = new RedisRateLimiter(redis);
        String bucket = bucket();
        int callers = 32;
        int limit = 10;
        ExecutorService pool = Executors.newFixedThreadPool(callers);
        CountDownLatch go = new CountDownLatch(1);
        var results = IntStream.range(0, callers).mapToObj(i -> pool.submit(() -> {
            go.await();
            return limiter.tryAcquire(bucket, "ip:1", limit, MINUTE);
        })).toList();
        go.countDown();
        long allowed = 0;
        for (Future<Boolean> result : results) {
            allowed += result.get() ? 1 : 0;
        }
        pool.shutdown();

        assertThat(allowed).isEqualTo(limit);
    }

    @Test
    void anUnreachableRedisFailsFastWithARuntimeException() {
        LettuceConnectionFactory unreachable = connectionTo("127.0.0.1", 1);
        RedisRateLimiter limiter = new RedisRateLimiter(new StringRedisTemplate(unreachable));
        long started = System.nanoTime();

        assertThatThrownBy(() -> limiter.tryAcquire(bucket(), "ip:1", 1, MINUTE))
                .isInstanceOf(RuntimeException.class);

        assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
        unreachable.destroy();
    }
}
