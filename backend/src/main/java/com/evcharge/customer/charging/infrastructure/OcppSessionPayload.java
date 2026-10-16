package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.RemoteSession;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppSessionPayload(
        int transactionId,
        String deviceId,
        int connectorId,
        String status,
        Instant startedAt,
        Instant stoppedAt,
        BigDecimal energyKwh,
        Long costMinor,
        String currency,
        Integer currencyDecimals,
        String stopReason) {

    RemoteSession toDomain() {
        return new RemoteSession(transactionId, deviceId, connectorId, status, startedAt, stoppedAt,
                energyKwh, costMinor, currency, currencyDecimals, stopReason);
    }
}
