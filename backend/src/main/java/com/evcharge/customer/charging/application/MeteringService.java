package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class MeteringService {

    private final ChargingGateway gateway;
    private final SessionRepository sessions;

    public MeteringService(ChargingGateway gateway, SessionRepository sessions) {
        this.gateway = gateway;
        this.sessions = sessions;
    }

    public Optional<Metering> of(ChargingSession session) {
        if (!session.state().isLive() || session.transactionId() == null) {
            return Optional.empty();
        }
        Optional<Metering> reading = gateway.metering(session.transactionId());
        reading.map(Metering::stateOfChargePercent).ifPresent(level -> sessions.recordBattery(session.id(), level));
        return reading;
    }

    public Optional<BigDecimal> batteryOf(ChargingSession session, Optional<Metering> reading) {
        return reading.map(Metering::stateOfChargePercent).or(() -> sessions.batteryOf(session.id()));
    }
}
