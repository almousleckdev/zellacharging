package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.PlatformEvent;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

@Service
public class PlatformEvents {

    private static final Logger log = LoggerFactory.getLogger(PlatformEvents.class);
    private static final int REMEMBERED_EVENTS = 5000;
    private static final Set<String> HANDLED = Set.of("session.metering", "session.started", "session.stopped");

    private final SessionRepository sessions;
    private final SessionTelemetry telemetry;
    private final SessionReconciler reconciler;
    private final CountryDirectory countries;
    private final Executor executor;
    private final Set<String> seen = Collections.synchronizedSet(Collections.newSetFromMap(
            new LinkedHashMap<String, Boolean>() {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Boolean> eldest) {
                    return size() > REMEMBERED_EVENTS;
                }
            }));

    public PlatformEvents(SessionRepository sessions, SessionTelemetry telemetry, SessionReconciler reconciler,
                          CountryDirectory countries, @Qualifier("platformEventExecutor") Executor executor) {
        this.sessions = sessions;
        this.telemetry = telemetry;
        this.reconciler = reconciler;
        this.countries = countries;
        this.executor = executor;
    }

    public void accept(PlatformEvent event) {
        if (!HANDLED.contains(event.type()) || !seen.add(event.eventId())) {
            return;
        }
        executor.execute(() -> handleSafely(event));
    }

    private void handleSafely(PlatformEvent event) {
        try {
            for (Country country : countries.active()) {
                if (handledIn(country, event)) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            log.warn("Platform event {} ({}) could not be applied: {}", event.eventId(), event.type(), e.getMessage());
        }
    }

    private boolean handledIn(Country country, PlatformEvent event) {
        return TenantContext.callAs(country.code(), () -> {
            Optional<ChargingSession> live = sessions.findLiveAt(event.deviceId(), event.connectorId(), event.idTag());
            live.ifPresent(session -> apply(session, event));
            return live.isPresent();
        });
    }

    private void apply(ChargingSession session, PlatformEvent event) {
        switch (event.type()) {
            case "session.metering" -> {
                if (event.metering() != null) {
                    telemetry.recordReading(session.id(), event.metering());
                }
            }
            case "session.started", "session.stopped" -> reconciler.reconcile(session);
            default -> {
            }
        }
    }
}
