package com.evcharge.customer.charging.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargingSession(
        UUID id,
        UUID customerId,
        String stationId,
        int connectorId,
        String idTag,
        SessionState state,
        String failureCode,
        Integer transactionId,
        Instant startedAt,
        Instant stoppedAt,
        BigDecimal energyKwh,
        Long costMinor,
        String currency,
        Integer currencyDecimals,
        String stopReason,
        Instant createdAt,
        Instant updatedAt) {

    public static ChargingSession requested(UUID id, UUID customerId, String stationId, int connectorId,
                                            String idTag, Instant now) {
        return new ChargingSession(id, customerId, stationId, connectorId, idTag, SessionState.REQUESTED,
                null, null, null, null, null, null, null, null, null, now, now);
    }

    public ChargingSession withState(SessionState next, String failure, Instant now) {
        return new ChargingSession(id, customerId, stationId, connectorId, idTag, next, failure, transactionId,
                startedAt, stoppedAt, energyKwh, costMinor, currency, currencyDecimals, stopReason, createdAt, now);
    }

    public ChargingSession withRemote(RemoteSession remote, SessionState next, Instant now) {
        return new ChargingSession(id, customerId, stationId, connectorId, idTag, next, failureCode,
                remote.transactionId(), remote.startedAt(), remote.stoppedAt(), remote.energyKwh(),
                remote.costMinor(), remote.currency(), remote.currencyDecimals(), remote.stopReason(),
                createdAt, now);
    }

    public ChargingSession withCost(long minor, String costCurrency, int decimals, Instant now) {
        return new ChargingSession(id, customerId, stationId, connectorId, idTag, state, failureCode, transactionId,
                startedAt, stoppedAt, energyKwh, minor, costCurrency, decimals, stopReason, createdAt, now);
    }
}
