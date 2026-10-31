package com.evcharge.customer.wallet.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.wallet.withdrawal")
public record WithdrawalDefaults(boolean enabled, long minimumMinor, long maximumMinor, long dailyMaximumMinor,
                                 Duration recheckAfter, Duration reviewAfter) {
}
