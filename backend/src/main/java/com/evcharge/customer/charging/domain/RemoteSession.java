package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record RemoteSession(
        int transactionId,
        String stationId,
        int connectorId,
        String status,
        Instant startedAt,
        Instant stoppedAt,
        BigDecimal energyKwh,
        Long costMinor,
        String currency,
        Integer currencyDecimals,
        String stopReason) {

    public SessionState toState() {
        return switch (status) {
            case "ACTIVE" -> SessionState.ACTIVE;
            case "COMPLETED" -> SessionState.COMPLETED;
            case "ABANDONED", "REJECTED" -> SessionState.ABANDONED;
            default -> SessionState.ACTIVE;
        };
    }
}
