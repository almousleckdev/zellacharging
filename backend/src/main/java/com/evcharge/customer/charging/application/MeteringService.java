package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.Metering;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MeteringService {

    private final ChargingGateway gateway;

    public MeteringService(ChargingGateway gateway) {
        this.gateway = gateway;
    }

    public Optional<Metering> of(ChargingSession session) {
        if (!session.state().isLive() || session.transactionId() == null) {
            return Optional.empty();
        }
        return gateway.metering(session.transactionId());
    }
}
