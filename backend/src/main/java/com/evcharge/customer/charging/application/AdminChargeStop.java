package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.ChargeStopper;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
public class AdminChargeStop implements ChargeStopper {

    private static final long ATTEMPT_SECONDS = 10;
    private static final String STILL_CHARGING = "ACTIVE";

    private final SessionRepository sessions;
    private final SessionRefresher refresher;
    private final UserStop userStop;
    private final ChargingGateway gateway;
    private final Clock clock;

    public AdminChargeStop(SessionRepository sessions, SessionRefresher refresher, UserStop userStop,
                    ChargingGateway gateway, Clock clock) {
        this.sessions = sessions;
        this.refresher = refresher;
        this.userStop = userStop;
        this.gateway = gateway;
        this.clock = clock;
    }

    @Override
    public void stopTransaction(int transactionId) {
        stop(sessions.findByTransactionId(transactionId).orElseThrow(AdminChargeStop::notFound));
    }

    @Override
    public void stop(UUID sessionId) {
        stop(sessions.findById(sessionId).orElseThrow(AdminChargeStop::notFound));
    }

    private static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found");
    }

    private void stop(ChargingSession found) {
        ChargingSession session = refresher.refresh(found);
        if (session.state() == SessionState.ACTIVE || session.state() == SessionState.STOPPING) {
            userStop.perform(session);
            return;
        }
        if (session.state() == SessionState.ABANDONED && session.transactionId() != null) {
            stopClosedCharge(session);
            return;
        }
        throw new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "No active charge to stop");
    }

    private void stopClosedCharge(ChargingSession session) {
        boolean running = gateway.session(session.transactionId()).map(remote -> STILL_CHARGING.equals(remote.status()))
                .orElse(false);
        if (!running) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "No active charge to stop");
        }
        String key = "admin-stop:" + session.id() + ":" + clock.instant().getEpochSecond() / ATTEMPT_SECONDS;
        if (!gateway.stop(session.stationId(), session.connectorId(), key).accepted()) {
            throw new ApiException(HttpStatus.CONFLICT, "STOP_REFUSED", "The charger refused to stop");
        }
    }
}
