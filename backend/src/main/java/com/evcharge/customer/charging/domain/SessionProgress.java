package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionProgress(BigDecimal energyKwh, Instant at) {

    public static SessionProgress none() {
        return new SessionProgress(null, null);
    }
}
