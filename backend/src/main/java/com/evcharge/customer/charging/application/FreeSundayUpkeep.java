package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.FreeSundayWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class FreeSundayUpkeep {

    private static final Logger log = LoggerFactory.getLogger(FreeSundayUpkeep.class);
    private static final Duration ORPHAN_AFTER = Duration.ofMinutes(10);
    private static final Duration ORPHAN_LOOK_BACK = Duration.ofDays(1);

    private final FreeWindowStopper stopper;
    private final FreeWindowReminder reminder;
    private final FreeSundayWindows windows;
    private final AuditTrail audit;
    private final Clock clock;

    public FreeSundayUpkeep(FreeWindowStopper stopper, FreeWindowReminder reminder, FreeSundayWindows windows,
                            AuditTrail audit, Clock clock) {
        this.stopper = stopper;
        this.reminder = reminder;
        this.windows = windows;
        this.audit = audit;
        this.clock = clock;
    }

    public void tend() {
        releaseOrphans();
        remindEndingSoon();
        stopExpired();
    }

    private void releaseOrphans() {
        try {
            Instant now = clock.instant();
            for (UUID session : windows.releaseOrphans(now.minus(ORPHAN_LOOK_BACK), now.minus(ORPHAN_AFTER))) {
                audit.record("FREE_SUNDAY_RELEASED", null, session.toString(), "ORPHAN_CLAIM");
                log.warn("Free Sunday claim of session {} had no session row: released", session);
            }
        } catch (RuntimeException e) {
            log.warn("Free Sunday orphan claim release failed", e);
        }
    }

    private void remindEndingSoon() {
        try {
            reminder.remindEndingSoon();
        } catch (RuntimeException e) {
            log.warn("Free Sunday closing notices failed", e);
        }
    }

    private void stopExpired() {
        try {
            stopper.stopExpired();
        } catch (RuntimeException e) {
            log.warn("Free Sunday window stop failed", e);
        }
    }
}
