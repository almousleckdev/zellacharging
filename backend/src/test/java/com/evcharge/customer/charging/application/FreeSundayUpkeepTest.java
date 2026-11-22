package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.FreeSundayWindows;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeSundayUpkeepTest {

    private static final Instant NOW = Instant.parse("2026-11-22T15:00:00Z");

    private final FreeWindowStopper stopper = mock(FreeWindowStopper.class);
    private final FreeWindowReminder reminder = mock(FreeWindowReminder.class);
    private final FreeSundayWindows windows = mock(FreeSundayWindows.class);
    private final AuditTrail audit = mock(AuditTrail.class);
    private final FreeSundayUpkeep upkeep = new FreeSundayUpkeep(stopper, reminder, windows, audit,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void releasesClaimsLeftWithoutSessionForMoreThanTenMinutes() {
        UUID orphan = UUID.randomUUID();
        when(windows.releaseOrphans(NOW.minus(Duration.ofDays(1)), NOW.minus(Duration.ofMinutes(10))))
                .thenReturn(List.of(orphan));

        upkeep.tend();

        verify(audit).record(eq("FREE_SUNDAY_RELEASED"), isNull(), eq(orphan.toString()), anyString());
        verify(stopper).stopExpired();
    }

    @Test
    void aFailingReleaseStillStopsExpiredWindows() {
        when(windows.releaseOrphans(any(), any())).thenThrow(new IllegalStateException("database hiccup"));

        upkeep.tend();

        verify(stopper).stopExpired();
    }

    @Test
    void aFailingStopNeverEscapesTheSweep() {
        when(windows.releaseOrphans(any(), any())).thenReturn(List.of());
        doThrow(new IllegalStateException("boom")).when(stopper).stopExpired();

        upkeep.tend();

        verify(windows).releaseOrphans(NOW.minus(Duration.ofDays(1)), NOW.minus(Duration.ofMinutes(10)));
    }

    @Test
    void remindersAreSentEveryRound() {
        upkeep.tend();

        verify(reminder).remindEndingSoon();
    }

    @Test
    void aFailingReminderStillStopsExpiredWindowsAndReleasesOrphans() {
        doThrow(new IllegalStateException("outbox down")).when(reminder).remindEndingSoon();

        upkeep.tend();

        verify(stopper).stopExpired();
    }
}
