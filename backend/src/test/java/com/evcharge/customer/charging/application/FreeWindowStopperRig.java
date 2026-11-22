package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.FreeSundayWindows;
import com.evcharge.customer.wallet.WalletFunding;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class FreeWindowStopperRig {

    static final Instant NOW = Instant.parse("2026-11-22T21:00:30Z");

    final FreeSundayWindows windows = mock(FreeSundayWindows.class);
    final SessionRepository sessions = mock(SessionRepository.class);
    final ChargingGateway gateway = mock(ChargingGateway.class);
    final AuditTrail audit = mock(AuditTrail.class);
    final WalletFunding funding = mock(WalletFunding.class);
    final ChargeAnnouncements announcements = mock(ChargeAnnouncements.class);
    final List<UUID> openHolds = new ArrayList<>();
    final FreeWindowStopper stopper;

    FreeWindowStopperRig() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        when(funding.openHoldSessions()).thenAnswer(invocation -> List.copyOf(openHolds));
        stopper = new FreeWindowStopper(windows, sessions, funding,
                new StopCommander(gateway, sessions, mock(ChargingMetrics.class), clock), announcements, audit, clock);
    }

    ChargingSession stored(SessionState state, String station) {
        ChargingSession session = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), station, 1, "Z-X", state,
                null, 4242, NOW.minusSeconds(3600), null, null, null, "XOF", 0, null, NOW.minusSeconds(3600),
                NOW.minusSeconds(60));
        when(sessions.findById(session.id())).thenReturn(Optional.of(session));
        when(funding.waived(session.id())).thenReturn(true);
        openHolds.add(session.id());
        return session;
    }

    void pastWindow(ChargingSession... found) {
        pastWindow(Arrays.stream(found).map(ChargingSession::id).toArray(UUID[]::new));
    }

    void pastWindow(UUID... ids) {
        when(windows.sessionsPastWindow(any(), eq(NOW))).thenReturn(List.of(ids));
    }

    void chargerAnswers(boolean accepted) {
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(accepted ? CommandOutcome.success()
                : CommandOutcome.refused("CHARGER_REFUSED", null));
    }
}
