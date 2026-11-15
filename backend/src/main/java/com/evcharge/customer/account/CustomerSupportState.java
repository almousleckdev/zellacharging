package com.evcharge.customer.account;

import java.time.Instant;

public record CustomerSupportState(Instant lockedUntil, int failedLoginAttempts, String pendingEmail,
                                   String pendingPhone) {
}
