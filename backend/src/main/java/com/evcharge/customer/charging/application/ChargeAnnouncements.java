package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ChargeAnnouncements {

    private static final Logger log = LoggerFactory.getLogger(ChargeAnnouncements.class);

    private final Notifier notifier;
    private final FreeSundayWindows windows;
    private final FreeWindowTimes times;

    public ChargeAnnouncements(Notifier notifier, FreeSundayWindows windows, FreeWindowTimes times) {
        this.notifier = notifier;
        this.windows = windows;
        this.times = times;
    }

    public void started(ChargingSession session) {
        Optional<Instant> windowEnd = windows.windowEnd(session.id());
        if (windowEnd.isPresent()) {
            notifier.notify(session.customerId(), NotificationType.FREE_CHARGE_STARTED,
                    "session:" + session.id() + ":" + NotificationType.FREE_CHARGE_STARTED,
                    params(session, times.display(windowEnd.get())));
            return;
        }
        notifier.notify(session.customerId(), NotificationType.CHARGE_STARTED,
                "session:" + session.id() + ":" + NotificationType.CHARGE_STARTED, params(session, null));
    }

    public void freeWindowEnding(ChargingSession session) {
        freeWindow(session, NotificationType.FREE_WINDOW_ENDING, "free-ending:" + session.id());
    }

    public void freeWindowEnded(ChargingSession session) {
        freeWindow(session, NotificationType.FREE_WINDOW_ENDED, "free-ended:" + session.id());
    }

    private void freeWindow(ChargingSession session, NotificationType type, String dedupeKey) {
        try {
            windows.windowEnd(session.id()).ifPresent(end -> notifier.notify(session.customerId(), type, dedupeKey,
                    params(session, times.display(end))));
        } catch (RuntimeException e) {
            log.warn("Notice {} for session {} could not be queued: {}", type, session.id(), e.getClass().getSimpleName());
        }
    }

    private static Map<String, String> params(ChargingSession session, String time) {
        Map<String, String> params = new HashMap<>(Map.of("screen", "charging", "id", session.id().toString()));
        if (time != null) {
            params.put("time", time);
        }
        return params;
    }
}
