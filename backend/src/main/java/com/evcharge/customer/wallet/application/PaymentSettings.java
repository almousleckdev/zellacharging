package com.evcharge.customer.wallet.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.payment")
public record PaymentSettings(String provider, boolean allowSandbox, Duration recheckAfter, Duration expireAfter,
                              Duration lateWindow) {
}
