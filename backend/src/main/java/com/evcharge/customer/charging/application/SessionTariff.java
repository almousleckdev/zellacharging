package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SessionTariff {

    private final SessionTelemetry telemetry;
    private final SessionSettings settings;

    public SessionTariff(SessionTelemetry telemetry, SessionSettings settings) {
        this.telemetry = telemetry;
        this.settings = settings;
    }

    public Optional<Long> pricePerKwhMinor(UUID sessionId) {
        return telemetry.priceOf(sessionId).or(() -> Optional.ofNullable(settings.pricePerKwhMinor()));
    }
}
