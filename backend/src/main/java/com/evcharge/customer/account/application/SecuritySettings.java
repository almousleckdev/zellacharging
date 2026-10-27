package com.evcharge.customer.account.application;

import java.time.Duration;

public record SecuritySettings(
        String jwtSecret,
        String codePepper,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        int maxFailedLogins,
        Duration lockout) {
}
