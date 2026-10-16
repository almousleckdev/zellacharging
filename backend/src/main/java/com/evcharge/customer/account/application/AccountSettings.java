package com.evcharge.customer.account.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("ndzela")
public record AccountSettings(Security security, Verification verification, String mailFrom) {

    public record Security(
            String jwtSecret,
            String codePepper,
            Duration accessTokenTtl,
            Duration refreshTokenTtl,
            int maxFailedLogins,
            Duration lockout) {
    }

    public record Verification(Duration codeTtl, Duration resendCooldown, int maxAttempts) {
    }
}
