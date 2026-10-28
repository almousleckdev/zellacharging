package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("zella.charging.stall")
public record StallSettings(Duration after) {

    public StallSettings {
        after = after == null ? Duration.ofMinutes(5) : after;
    }
}
