package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.TransactionClaims;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrphanChargeGuard {

    private static final Logger log = LoggerFactory.getLogger(OrphanChargeGuard.class);

    private final ChargingGateway gateway;
    private final RemoteSessionLookup lookup;
    private final OrphanChargeAdopter adopter;
    private final TransactionClaims claims;
    private final SessionRepository sessions;

    public OrphanChargeGuard(ChargingGateway gateway, RemoteSessionLookup lookup, OrphanChargeAdopter adopter,
                             TransactionClaims claims, SessionRepository sessions) {
        this.gateway = gateway;
        this.lookup = lookup;
        this.adopter = adopter;
        this.claims = claims;
        this.sessions = sessions;
    }

    public int stopOrphans() {
        int stopped = 0;
        for (RemoteSession remote : lookup.activeSessions()) {
            if (isOrphan(remote) && !adopted(remote)) {
                stop(remote);
                stopped++;
            }
        }
        return stopped;
    }

    private boolean isOrphan(RemoteSession remote) {
        if (remote.toState() != SessionState.ACTIVE || remote.idTag() == null || !claims.knownIdTag(remote.idTag())) {
            return false;
        }
        boolean tracked = claims.heldByAnother(remote.transactionId(), UUID.randomUUID())
                || sessions.findLiveAt(remote.stationId(), remote.connectorId(), remote.idTag()).isPresent();
        return !tracked;
    }

    private boolean adopted(RemoteSession remote) {
        try {
            return adopter.adopt(remote);
        } catch (RuntimeException e) {
            log.warn("Adoption of untracked transaction {} failed", remote.transactionId(), e);
            return false;
        }
    }

    private void stop(RemoteSession remote) {
        log.error("ALERT transaction {} at {} connector {} is charging with no session of ours: stopping it",
                remote.transactionId(), remote.stationId(), remote.connectorId());
        try {
            gateway.stop(remote.stationId(), remote.connectorId(), "orphan:" + remote.transactionId() + ":" + java.time.Instant.now().getEpochSecond() / 10);
        } catch (RuntimeException e) {
            log.warn("Stop of untracked transaction {} could not be sent", remote.transactionId(), e);
        }
    }
}
