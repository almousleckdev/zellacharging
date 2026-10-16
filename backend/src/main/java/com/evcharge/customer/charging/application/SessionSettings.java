package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("ndzela.charging")
public record SessionSettings(
        Duration plugInTimeout,
        Duration sweepInterval,
        Duration stopRetryAfter,
        Long pricePerKwhMinor,
        int stopThresholdPercent) {
}
