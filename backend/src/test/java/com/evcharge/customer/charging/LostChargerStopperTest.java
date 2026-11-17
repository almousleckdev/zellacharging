package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.LostChargerStopper;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LostChargerStopperTest {

    private static final Instant NOW = Instant.parse("2026-11-17T13:20:00Z");

    private final SessionRepository sessions = mock(SessionRepository.class);
    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final LostChargerStopper stopper = new LostChargerStopper(sessions, gateway,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession closed() {
        Instant at = NOW.minus(Duration.ofMinutes(5));
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "DY2", 2, "Z-X", SessionState.ABANDONED,
                "CHARGER_OFFLINE", 951, at, at, null, 8740L, "XAF", 0, "PowerLoss", at, at);
    }

    private RemoteSession remote(String status) {
        return new RemoteSession(951, "DY2", 2, status, null, null, null, null, null, null, null, null, null, null,
                null);
    }

    @Test
    void stopsTheChargerWhenThePlatformStillSeesTheTransactionRunning() {
        when(sessions.closedWithoutCharger(any())).thenReturn(List.of(closed()));
        when(gateway.session(951)).thenReturn(Optional.of(remote("ACTIVE")));
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(new CommandOutcome(true, null, null));

        stopper.stopWhatIsStillCharging();

        verify(gateway).stop(anyString(), anyInt(), anyString());
    }

    @Test
    void leavesAChargerAloneOnceThePlatformShowsTheTransactionEnded() {
        when(sessions.closedWithoutCharger(any())).thenReturn(List.of(closed()));
        when(gateway.session(951)).thenReturn(Optional.of(remote("COMPLETED")));

        stopper.stopWhatIsStillCharging();

        verify(gateway, never()).stop(anyString(), anyInt(), anyString());
    }
}
