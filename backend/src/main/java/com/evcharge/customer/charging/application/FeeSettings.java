package com.evcharge.customer.charging.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties("ndzela.fees")
public record FeeSettings(BigDecimal vatPercent, BigDecimal serviceFeePercent) {

    public FeeSettings {
        vatPercent = vatPercent == null ? BigDecimal.ZERO : vatPercent;
        serviceFeePercent = serviceFeePercent == null ? BigDecimal.ZERO : serviceFeePercent;
    }
}
