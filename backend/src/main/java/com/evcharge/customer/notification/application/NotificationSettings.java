package com.evcharge.customer.notification.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.notifications")
public record NotificationSettings(int batchSize, int maxAttempts, Duration retryBase, Duration retention) {
}
