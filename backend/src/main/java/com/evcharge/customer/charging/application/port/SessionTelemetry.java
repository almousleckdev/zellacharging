package com.evcharge.customer.charging.application.port;

import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionProgress;

import java.util.Optional;
import java.util.UUID;

public interface SessionTelemetry {

    void recordReading(UUID id, Metering reading);

    LastReading lastReadingOf(UUID id);

    SessionProgress progressOf(UUID id);

    void recordPrice(UUID id, long pricePerKwhMinor);

    Optional<Long> priceOf(UUID id);
}
