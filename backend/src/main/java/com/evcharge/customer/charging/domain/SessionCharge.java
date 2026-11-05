package com.evcharge.customer.charging.domain;

import java.time.Instant;
import java.util.UUID;

public record SessionCharge(UUID sessionId, UUID customerId, ChargeState state, Long energyCostMinor, Long vatMinor,
                            Long serviceFeeMinor, Long totalMinor, String currency, Integer decimals,
                            boolean estimated, Instant updatedAt) {

    public static SessionCharge charged(ChargingSession session, FeeBreakdown fees, long capturedMinor,
                                        boolean estimated, Instant now) {
        return new SessionCharge(session.id(), session.customerId(), ChargeState.CHARGED, fees.electricMinor(),
                fees.vatMinor(), fees.serviceMinor(), capturedMinor, session.currency(), session.currencyDecimals(),
                estimated, now);
    }

    public static SessionCharge settled(ChargingSession session, FeeBreakdown fees, long capturedMinor,
                                        boolean estimated, Instant now) {
        if (capturedMinor > 0) {
            return charged(session, fees, capturedMinor, estimated, now);
        }
        ChargeState state = fees.totalMinor() == 0 ? ChargeState.NOT_CHARGED : ChargeState.UNDER_REVIEW;
        return withoutAmounts(session, state, estimated, now);
    }

    public static SessionCharge withoutAmounts(ChargingSession session, ChargeState state, boolean estimated,
                                               Instant now) {
        return new SessionCharge(session.id(), session.customerId(), state, null, null, null, null,
                session.currency(), session.currencyDecimals(), estimated, now);
    }
}
