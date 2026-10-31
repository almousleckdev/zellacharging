package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties("zella.fees")
public record FeeSettings(BigDecimal vatPercent, BigDecimal serviceFeePercent) implements FeeRates {

    public FeeSettings {
        vatPercent = vatPercent == null ? BigDecimal.ZERO : vatPercent;
        serviceFeePercent = serviceFeePercent == null ? BigDecimal.ZERO : serviceFeePercent;
    }
}
