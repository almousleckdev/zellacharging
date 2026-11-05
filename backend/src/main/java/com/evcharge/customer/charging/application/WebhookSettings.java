package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.ocpp-webhook")
public record WebhookSettings(String secret, Duration tolerance) {
}
