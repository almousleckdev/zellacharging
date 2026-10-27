package com.evcharge.customer.shared;

import java.time.Duration;

public interface RateLimiter {

    boolean tryAcquire(String bucket, String key, int limit, Duration window);
}
