package com.evcharge.customer.wallet.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("zella.wallet")
public record WalletSettings(
        String currency,
        int decimals,
        long minimumStartMinor,
        long maximumStartMinor,
        long minTopupMinor,
        long maxTopupMinor) {
}
