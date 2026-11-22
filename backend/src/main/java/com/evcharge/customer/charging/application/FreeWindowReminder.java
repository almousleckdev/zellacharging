package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class FreeWindowReminder {

    static final Duration NOTICE = Duration.ofMinutes(30);

    private static final Logger log = LoggerFactory.getLogger(FreeWindowReminder.class);

    private final FreeSundayWindows windows;
    private final SessionRepository sessions;
    private final WalletFunding funding;
    private final ChargeAnnouncements announcements;
    private final Clock clock;

    public FreeWindowReminder(FreeSundayWindows windows, SessionRepository sessions, WalletFunding funding,
                              ChargeAnnouncements announcements, Clock clock) {
        this.windows = windows;
        this.sessions = sessions;
        this.funding = funding;
        this.announcements = announcements;
        this.clock = clock;
    }

    public void remindEndingSoon() {
        Instant now = clock.instant();
        for (UUID id : windows.sessionsEndingBetween(now, now.plus(NOTICE))) {
            try {
                sessions.findById(id).filter(session -> session.state() == SessionState.ACTIVE)
                        .filter(session -> funding.waived(session.id())).ifPresent(this::tell);
            } catch (RuntimeException e) {
                log.warn("Closing notice check failed for session {}", id, e);
            }
        }
    }

    private void tell(ChargingSession session) {
        announcements.freeWindowEnding(session);
    }
}
