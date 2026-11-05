package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionCharges;
import com.evcharge.customer.charging.domain.ChargeState;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.FeeBreakdown;
import com.evcharge.customer.charging.domain.SessionCharge;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class SessionChargeRecorder {

    private final SessionCharges charges;
    private final Clock clock;

    public SessionChargeRecorder(SessionCharges charges, Clock clock) {
        this.charges = charges;
        this.clock = clock;
    }

    public void settled(ChargingSession session, FeeBreakdown fees, long capturedMinor) {
        charges.insertIfAbsent(SessionCharge.settled(session, fees, capturedMinor, false, clock.instant()));
    }

    public void notCharged(ChargingSession session) {
        charges.insertIfAbsent(SessionCharge.withoutAmounts(session, ChargeState.NOT_CHARGED, false,
                clock.instant()));
    }

    public void underReview(ChargingSession session) {
        charges.insertIfAbsent(SessionCharge.withoutAmounts(session, ChargeState.UNDER_REVIEW, false,
                clock.instant()));
    }
}
