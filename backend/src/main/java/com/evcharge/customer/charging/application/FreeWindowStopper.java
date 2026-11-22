package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class FreeWindowStopper {

    static final String REASON = "FREE_WINDOW_ENDED";
    static final Duration LOOK_BACK = Duration.ofHours(24);
    static final int MAX_STOPS_PER_SWEEP = 5;

    private static final Logger log = LoggerFactory.getLogger(FreeWindowStopper.class);

    private final FreeSundayWindows windows;
    private final SessionRepository sessions;
    private final WalletFunding funding;
    private final StopCommander stopper;
    private final ChargeAnnouncements announcements;
    private final AuditTrail audit;
    private final Clock clock;
    private final Map<UUID, Instant> lastTried = new ConcurrentHashMap<>();

    public FreeWindowStopper(FreeSundayWindows windows, SessionRepository sessions, WalletFunding funding,
                             StopCommander stopper, ChargeAnnouncements announcements, AuditTrail audit,
                             Clock clock) {
        this.windows = windows;
        this.sessions = sessions;
        this.funding = funding;
        this.stopper = stopper;
        this.announcements = announcements;
        this.audit = audit;
        this.clock = clock;
    }

    public void stopExpired() {
        Instant now = clock.instant();
        List<ChargingSession> charging = stillCharging(windows.sessionsPastWindow(now.minus(LOOK_BACK), now));
        lastTried.keySet().retainAll(charging.stream().map(ChargingSession::id).collect(Collectors.toSet()));
        charging.stream()
                .sorted(Comparator.comparing(session -> lastTried.getOrDefault(session.id(), Instant.MIN)))
                .limit(MAX_STOPS_PER_SWEEP)
                .forEach(session -> attempt(session, now));
    }

    private List<ChargingSession> stillCharging(List<UUID> ids) {
        List<ChargingSession> charging = new ArrayList<>();
        for (UUID id : ids) {
            try {
                sessions.findById(id).filter(session -> session.state() == SessionState.ACTIVE)
                        .filter(session -> funding.waived(id)).ifPresent(charging::add);
            } catch (RuntimeException e) {
                log.warn("Free window check failed for session {}", id, e);
            }
        }
        return charging;
    }

    private void attempt(ChargingSession session, Instant now) {
        lastTried.put(session.id(), now);
        try {
            stop(session);
        } catch (RuntimeException e) {
            log.error("ALERT free Sunday session {} is past its window and could not be stopped: retrying "
                    + "next sweep", session.id(), e);
        }
    }

    private void stop(ChargingSession candidate) {
        Optional<ChargingSession> current = sessions.findById(candidate.id())
                .filter(session -> session.state() == SessionState.ACTIVE)
                .filter(session -> funding.openHoldSessions().contains(session.id()));
        if (current.isEmpty()) {
            log.info("Free Sunday session {} ended before its window stop: skipped", candidate.id());
            return;
        }
        ChargingSession session = current.get();
        if (stopper.issue(session.withStopReason(REASON)).accepted()) {
            audit.record("FREE_SUNDAY_WINDOW_STOPPED", null, session.id().toString(),
                    "customer=" + session.customerId());
            log.info("Free Sunday session {} stopped at the end of its window", session.id());
            announcements.freeWindowEnded(session);
            return;
        }
        log.error("ALERT free Sunday session {} is past its window and the charger refused to stop: retrying "
                + "next sweep", session.id());
    }
}
