package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RemoteSessionLookup {

    private static final Logger log = LoggerFactory.getLogger(RemoteSessionLookup.class);

    private record Snapshot(Instant at, List<RemoteSession> sessions) {
    }

    private static final long CLOCK_SKEW_SECONDS = 120;
    private static final Duration FALLBACK_INTERVAL = Duration.ofSeconds(20);

    private final ChargingGateway gateway;
    private final SessionSettings settings;
    private final Clock clock;
    private final Map<UUID, Instant> fallbackAt = new ConcurrentHashMap<>();
    private Snapshot snapshot = new Snapshot(Instant.MIN, List.of());

    public RemoteSessionLookup(ChargingGateway gateway, SessionSettings settings, Clock clock) {
        this.gateway = gateway;
        this.settings = settings;
        this.clock = clock;
    }

    public Optional<RemoteSession> find(ChargingSession session) {
        Optional<RemoteSession> running = match(activeSnapshot(), session);
        if (running.isPresent()) {
            return running;
        }
        if (session.transactionId() != null) {
            Optional<RemoteSession> known = gateway.session(session.transactionId())
                    .filter(remote -> matches(remote, session));
            if (known.isPresent()) {
                return known;
            }
        }
        return viaIdTag(session);
    }

    private synchronized List<RemoteSession> activeSnapshot() {
        Instant now = clock.instant();
        if (snapshot.at().plus(settings.userRefreshInterval()).isAfter(now)) {
            return snapshot.sessions();
        }
        snapshot = new Snapshot(now, gateway.activeSessions());
        return snapshot.sessions();
    }

    private Optional<RemoteSession> viaIdTag(ChargingSession session) {
        Instant now = clock.instant();
        Instant last = fallbackAt.get(session.id());
        boolean expiring = session.createdAt().plus(settings.plugInTimeout()).minus(FALLBACK_INTERVAL).isBefore(now);
        if (last != null && last.plus(FALLBACK_INTERVAL).isAfter(now) && !expiring) {
            return Optional.empty();
        }
        fallbackAt.put(session.id(), now);
        List<RemoteSession> ofIdTag = gateway.sessionsOf(session.idTag());
        Optional<RemoteSession> found = match(ofIdTag, session);
        if (found.isEmpty()) {
            logUnmatched(session, ofIdTag);
        }
        return found;
    }

    private void logUnmatched(ChargingSession session, List<RemoteSession> ofIdTag) {
        log.warn("No platform session matches charge {} (station={}, connector={}, idTag={}, requestedAt={}); "
                        + "platform sessions of this idTag: {}; active on this station: {}",
                session.id(), session.stationId(), session.connectorId(), session.idTag(), session.createdAt(),
                describe(ofIdTag.stream()), describe(snapshot.sessions().stream()
                        .filter(remote -> session.stationId().equals(remote.stationId()))));
    }

    private static String describe(java.util.stream.Stream<RemoteSession> sessions) {
        return sessions.map(remote -> "[tx=" + remote.transactionId() + " station=" + remote.stationId()
                + " connector=" + remote.connectorId() + " idTag=" + remote.idTag()
                + " startedAt=" + remote.startedAt() + "]").toList().toString();
    }

    private static Optional<RemoteSession> match(List<RemoteSession> candidates, ChargingSession session) {
        return candidates.stream().filter(remote -> matches(remote, session))
                .max(Comparator.comparing(RemoteSession::startedAt));
    }

    private static boolean matches(RemoteSession remote, ChargingSession session) {
        boolean sameOwner = remote.idTag() == null || remote.idTag().equals(session.idTag());
        return sameOwner && remote.stationId().equals(session.stationId())
                && remote.connectorId() == session.connectorId()
                && remote.startedAt() != null && startedAfterRequest(remote, session);
    }

    private static boolean startedAfterRequest(RemoteSession remote, ChargingSession session) {
        boolean stillOpen = remote.toState() == SessionState.ACTIVE;
        Instant earliest = session.createdAt().minusSeconds(CLOCK_SKEW_SECONDS);
        return stillOpen || !remote.startedAt().isBefore(earliest);
    }
}
