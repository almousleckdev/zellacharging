package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRefresher {

    private final SessionReconciler reconciler;
    private final SessionSettings settings;
    private final Clock clock;
    private final Map<UUID, Instant> refreshedAt = new ConcurrentHashMap<>();

    public SessionRefresher(SessionReconciler reconciler, SessionSettings settings, Clock clock) {
        this.reconciler = reconciler;
        this.settings = settings;
        this.clock = clock;
    }

    public ChargingSession refresh(ChargingSession session) {
        if (!session.state().isLive()) {
            refreshedAt.remove(session.id());
            return session;
        }
        Instant now = clock.instant();
        Instant last = refreshedAt.get(session.id());
        if (last != null && last.plus(settings.userRefreshInterval()).isAfter(now)) {
            return session;
        }
        refreshedAt.put(session.id(), now);
        return reconciler.reconcile(session);
    }
}
