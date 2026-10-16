package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class StopCommander {

    private final ChargingGateway gateway;
    private final SessionRepository sessions;
    private final Clock clock;

    public StopCommander(ChargingGateway gateway, SessionRepository sessions, Clock clock) {
        this.gateway = gateway;
        this.sessions = sessions;
        this.clock = clock;
    }

    public CommandOutcome issue(ChargingSession session) {
        CommandOutcome outcome = gateway.stop(session.stationId(), session.connectorId(),
                "stop:" + session.id() + ":" + clock.instant().toEpochMilli());
        if (outcome.accepted()) {
            sessions.update(session.withState(SessionState.STOPPING, null, clock.instant()));
        }
        return outcome;
    }
}
