package com.evcharge.customer.account.application;

import java.time.Duration;

public record VerificationSettings(Duration codeTtl, Duration resendCooldown, int maxAttempts) {
}
