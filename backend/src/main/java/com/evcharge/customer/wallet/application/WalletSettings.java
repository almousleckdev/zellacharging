package com.evcharge.customer.wallet.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ndzela.wallet")
public record WalletSettings(
        String currency,
        int decimals,
        long minimumStartMinor,
        long minTopupMinor,
        long maxTopupMinor) {
}
