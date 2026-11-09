package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.ApiException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

@Component
public class ChargerSilence {

    private final SessionTelemetry telemetry;
    private final ChargingGateway gateway;
    private final StallSettings settings;
    private final Clock clock;

    public ChargerSilence(SessionTelemetry telemetry, ChargingGateway gateway, StallSettings settings,
                          Clock clock) {
        this.telemetry = telemetry;
        this.gateway = gateway;
        this.settings = settings;
        this.clock = clock;
    }

    public boolean confirmed(ChargingSession session) {
        return confirmed(session, settings.silentAfter());
    }

    public boolean confirmed(ChargingSession session, Duration quietFor) {
        if (session.transactionId() == null || !silent(session, quietFor)) {
            return false;
        }
        try {
            gateway.metering(session.transactionId()).ifPresent(found -> telemetry.recordReading(session.id(), found));
        } catch (ApiException e) {
            return false;
        }
        return silent(session, quietFor);
    }

    private boolean silent(ChargingSession session, Duration quietFor) {
        return telemetry.readingAtOf(session.id())
                .map(at -> at.plus(quietFor).isBefore(clock.instant()))
                .orElse(false);
    }
}
