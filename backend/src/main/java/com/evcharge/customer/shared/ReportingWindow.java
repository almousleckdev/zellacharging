package com.evcharge.customer.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.time.OffsetDateTime;

@ConfigurationProperties("zella.reporting")
public record ReportingWindow(Instant start) {

    public OffsetDateTime since() {
        return Timestamps.toDb(start == null ? Instant.EPOCH : start);
    }
}
