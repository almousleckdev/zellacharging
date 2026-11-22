package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeWindowReminderTest {

    private static final Instant NOW = Instant.parse("2026-11-22T22:35:00Z");

    private final FreeSundayWindows windows = mock(FreeSundayWindows.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final WalletFunding funding = mock(WalletFunding.class);
    private final ChargeAnnouncements announcements = mock(ChargeAnnouncements.class);
    private final FreeWindowReminder reminder = new FreeWindowReminder(windows, sessions, funding, announcements,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession stored(SessionState state) {
        ChargingSession session = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-X", state,
                null, 7, NOW.minusSeconds(3600), null, null, null, "XAF", 0, null, NOW.minusSeconds(3600), NOW);
        when(sessions.findById(session.id())).thenReturn(Optional.of(session));
        when(funding.waived(session.id())).thenReturn(true);
        return session;
    }

    private void endingSoon(ChargingSession... found) {
        when(windows.sessionsEndingBetween(NOW, NOW.plus(Duration.ofMinutes(30))))
                .thenReturn(java.util.Arrays.stream(found).map(ChargingSession::id).toList());
    }

    @Test
    void aChargingFreeSessionWhoseWindowEndsWithinThirtyMinutesIsToldOnce() {
        ChargingSession session = stored(SessionState.ACTIVE);
        endingSoon(session);

        reminder.remindEndingSoon();

        verify(announcements).freeWindowEnding(session);
    }

    @Test
    void theLookedUpRangeIsFromNowToThirtyMinutesFromNow() {
        when(windows.sessionsEndingBetween(any(), any())).thenReturn(List.of());

        reminder.remindEndingSoon();

        verify(windows).sessionsEndingBetween(NOW, NOW.plus(Duration.ofMinutes(30)));
    }

    @Test
    void sessionsThatAreNotChargingAnymoreAreLeftAlone() {
        endingSoon(stored(SessionState.STOPPING), stored(SessionState.COMPLETED), stored(SessionState.FAILED),
                stored(SessionState.START_ACCEPTED));

        reminder.remindEndingSoon();

        verify(announcements, never()).freeWindowEnding(any());
    }

    @Test
    void aSessionWhoseHoldIsNotWaivedIsLeftAlone() {
        ChargingSession session = stored(SessionState.ACTIVE);
        when(funding.waived(session.id())).thenReturn(false);
        endingSoon(session);

        reminder.remindEndingSoon();

        verify(announcements, never()).freeWindowEnding(any());
    }

    @Test
    void oneFailingSessionDoesNotStopTheOthers() {
        ChargingSession broken = stored(SessionState.ACTIVE);
        ChargingSession fine = stored(SessionState.ACTIVE);
        doThrow(new IllegalStateException("boom")).when(announcements).freeWindowEnding(broken);
        endingSoon(broken, fine);

        reminder.remindEndingSoon();

        verify(announcements).freeWindowEnding(fine);
    }
}
