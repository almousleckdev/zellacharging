package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChargeAnnouncementsTest {

    private static final Instant NOW = Instant.parse("2026-11-22T15:00:00Z");
    private static final Instant END = Instant.parse("2026-11-22T23:00:00Z");

    private final Notifier notifier = mock(Notifier.class);
    private final FreeSundayWindows windows = mock(FreeSundayWindows.class);
    private final FreeWindowTimes times = mock(FreeWindowTimes.class);
    private final ChargeAnnouncements announcements = new ChargeAnnouncements(notifier, windows, times);

    private ChargingSession session() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", SessionState.ACTIVE, null,
                7, NOW, null, null, null, "XAF", 0, null, NOW, NOW);
    }

    @Test
    void aPaidChargeIsAnnouncedAsAnOrdinaryChargeStart() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.empty());

        announcements.started(session);

        verify(notifier).notify(session.customerId(), NotificationType.CHARGE_STARTED,
                "session:" + session.id() + ":CHARGE_STARTED", Map.of("screen", "charging", "id", session.id().toString()));
    }

    @Test
    void aFreeChargeIsAnnouncedAsFreeWithItsEndTimeAndNeverAsAnOrdinaryStart() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.of(END));
        when(times.display(END)).thenReturn("23:59");

        announcements.started(session);

        verify(notifier).notify(session.customerId(), NotificationType.FREE_CHARGE_STARTED,
                "session:" + session.id() + ":FREE_CHARGE_STARTED",
                Map.of("screen", "charging", "id", session.id().toString(), "time", "23:59"));
        verify(notifier, never()).notify(any(), eq(NotificationType.CHARGE_STARTED), anyString(), any());
    }

    @Test
    void theClosingNoticeIsSentOncePerSessionWithItsEndTime() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.of(END));
        when(times.display(END)).thenReturn("23:59");

        announcements.freeWindowEnding(session);

        verify(notifier).notify(session.customerId(), NotificationType.FREE_WINDOW_ENDING,
                "free-ending:" + session.id(),
                Map.of("screen", "charging", "id", session.id().toString(), "time", "23:59"));
    }

    @Test
    void theStopNoticeIsSentOncePerSessionWithItsEndTime() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.of(END));
        when(times.display(END)).thenReturn("23:59");

        announcements.freeWindowEnded(session);

        verify(notifier).notify(session.customerId(), NotificationType.FREE_WINDOW_ENDED,
                "free-ended:" + session.id(),
                Map.of("screen", "charging", "id", session.id().toString(), "time", "23:59"));
    }

    @Test
    void aSessionWithoutAClaimGetsNeitherClosingNotice() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.empty());

        announcements.freeWindowEnding(session);
        announcements.freeWindowEnded(session);

        verify(notifier, never()).notify(any(), any(), anyString(), any());
    }

    @Test
    void aFailingNotifierNeverBreaksTheCaller() {
        ChargingSession session = session();
        when(windows.windowEnd(session.id())).thenReturn(Optional.of(END));
        when(times.display(END)).thenReturn("23:59");
        org.mockito.Mockito.doThrow(new IllegalStateException("outbox down")).when(notifier)
                .notify(any(), eq(NotificationType.FREE_WINDOW_ENDED), anyString(), any());

        announcements.freeWindowEnded(session);
    }
}
