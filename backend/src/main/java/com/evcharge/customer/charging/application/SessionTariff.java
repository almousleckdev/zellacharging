package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SessionTariff {

    private final SessionTelemetry telemetry;
    private final PriceList prices;

    public SessionTariff(SessionTelemetry telemetry, PriceList prices) {
        this.telemetry = telemetry;
        this.prices = prices;
    }

    public Optional<Long> pricePerKwhMinor(UUID sessionId) {
        return telemetry.priceOf(sessionId).or(() -> Optional.ofNullable(prices.pricePerKwhMinor()));
    }
}
