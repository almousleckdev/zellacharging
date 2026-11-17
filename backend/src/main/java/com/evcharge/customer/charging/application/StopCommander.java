package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class StopCommander {

    private static final Logger log = LoggerFactory.getLogger(StopCommander.class);
    private static final long ATTEMPT_SECONDS = 10;

    private final ChargingGateway gateway;
    private final SessionRepository sessions;
    private final ChargingMetrics metrics;
    private final Clock clock;

    public StopCommander(ChargingGateway gateway, SessionRepository sessions, ChargingMetrics metrics,
                         Clock clock) {
        this.gateway = gateway;
        this.sessions = sessions;
        this.metrics = metrics;
        this.clock = clock;
    }

    public CommandOutcome issue(ChargingSession session) {
        long attempt = clock.instant().getEpochSecond() / ATTEMPT_SECONDS;
        CommandOutcome outcome = gateway.stop(session.stationId(), session.connectorId(),
                "stop:" + session.id() + ":" + attempt);
        if (outcome.accepted()) {
            sessions.update(session.withState(SessionState.STOPPING, null, clock.instant()));
        } else {
            metrics.stopRefused();
            log.error("ALERT stop not accepted for session {} at {} connector {}: {} {}", session.id(),
                    session.stationId(), session.connectorId(), outcome.reasonCode(), outcome.message());
        }
        return outcome;
    }
}
