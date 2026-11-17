package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

@Component
public class LostChargerStopper {

    private static final Logger log = LoggerFactory.getLogger(LostChargerStopper.class);
    private static final Duration WINDOW = Duration.ofHours(3);
    private static final String STILL_CHARGING = "ACTIVE";

    private final SessionRepository sessions;
    private final ChargingGateway gateway;
    private final Clock clock;

    public LostChargerStopper(SessionRepository sessions, ChargingGateway gateway, Clock clock) {
        this.sessions = sessions;
        this.gateway = gateway;
        this.clock = clock;
    }

    public void stopWhatIsStillCharging() {
        for (ChargingSession session : sessions.closedWithoutCharger(clock.instant().minus(WINDOW))) {
            try {
                stopIfStillCharging(session);
            } catch (RuntimeException e) {
                log.warn("Could not check the closed session {} on its charger", session.id(), e);
            }
        }
    }

    private void stopIfStillCharging(ChargingSession session) {
        Optional<RemoteSession> remote = gateway.session(session.transactionId());
        if (remote.isEmpty() || !STILL_CHARGING.equals(remote.get().status())) {
            return;
        }
        long attempt = clock.instant().getEpochSecond() / 60;
        boolean accepted = gateway.stop(session.stationId(), session.connectorId(),
                "lost-stop:" + session.id() + ":" + attempt).accepted();
        log.error("ALERT session {} is closed but {} connector {} is still charging: stop {}", session.id(),
                session.stationId(), session.connectorId(), accepted ? "accepted" : "refused");
    }
}
