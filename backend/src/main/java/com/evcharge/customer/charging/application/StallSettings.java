package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.charging.stall")
public record StallSettings(Duration after, Duration silentAfter) {

    private static final Duration MINIMUM_SILENCE = Duration.ofSeconds(150);

    public StallSettings {
        after = after == null ? Duration.ofMinutes(5) : after;
        silentAfter = silentAfter == null || silentAfter.compareTo(MINIMUM_SILENCE) < 0
                ? MINIMUM_SILENCE : silentAfter;
    }
}
