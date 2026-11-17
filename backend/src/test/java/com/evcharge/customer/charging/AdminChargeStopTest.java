package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.AdminChargeStop;
import com.evcharge.customer.charging.application.SessionRefresher;
import com.evcharge.customer.charging.application.UserStop;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminChargeStopTest {

    private static final Instant NOW = Instant.parse("2026-11-17T19:00:00Z");

    private final SessionRepository sessions = mock(SessionRepository.class);
    private final SessionRefresher refresher = mock(SessionRefresher.class);
    private final UserStop userStop = mock(UserStop.class);
    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final AdminChargeStop stop = new AdminChargeStop(sessions, refresher, userStop, gateway,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession session(SessionState state) {
        ChargingSession session = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "DY1", 2, "Z-X", state,
                null, 960, NOW.minusSeconds(600), null, null, null, null, null, null, NOW.minusSeconds(700), NOW);
        when(sessions.findById(session.id())).thenReturn(Optional.of(session));
        when(refresher.refresh(session)).thenReturn(session);
        return session;
    }

    @Test
    void anActiveChargeIsStoppedThroughTheSameStopAsTheCustomer() {
        ChargingSession session = session(SessionState.ACTIVE);

        stop.stop(session.id());

        verify(userStop).perform(session);
    }

    @Test
    void aClosedChargeThatKeepsRunningIsStoppedOnTheCharger() {
        ChargingSession session = session(SessionState.ABANDONED);
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(CommandOutcome.success());

        stop.stop(session.id());

        verify(gateway).stop(anyString(), anyInt(), anyString());
        verify(userStop, never()).perform(any());
    }

    @Test
    void aRefusedStopOnAClosedChargeIsReported() {
        ChargingSession session = session(SessionState.ABANDONED);
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(CommandOutcome.refused("CHARGER_REFUSED", null));

        assertThatThrownBy(() -> stop.stop(session.id())).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo("STOP_REFUSED");
    }

    @Test
    void aChargeIsFoundByItsTransactionForThePartner() {
        ChargingSession session = session(SessionState.ACTIVE);
        when(sessions.findByTransactionId(960)).thenReturn(Optional.of(session));

        stop.stopTransaction(960);

        verify(userStop).perform(session);
    }

    @Test
    void anUnknownTransactionIsNotFound() {
        when(sessions.findByTransactionId(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stop.stopTransaction(1)).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo("SESSION_NOT_FOUND");
    }

    @Test
    void aFinishedChargeCannotBeStopped() {
        ChargingSession session = session(SessionState.COMPLETED);

        assertThatThrownBy(() -> stop.stop(session.id())).isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).code()).isEqualTo("SESSION_NOT_ACTIVE");
        assertThat(session.state()).isEqualTo(SessionState.COMPLETED);
    }
}
