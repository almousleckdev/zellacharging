package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.ChargingMetrics;
import com.evcharge.customer.charging.application.StopCommander;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StopCommanderTest {

    private static final Instant START = Instant.parse("2026-11-17T14:17:30Z");

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final SessionRepository sessions = mock(SessionRepository.class);

    private StopCommander at(Instant now) {
        return new StopCommander(gateway, sessions, mock(ChargingMetrics.class), Clock.fixed(now, ZoneOffset.UTC));
    }

    private ChargingSession active() {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "DY5", 2, "Z-X", SessionState.ACTIVE, null,
                955, START, null, null, null, "XAF", 0, null, START, START);
    }

    @Test
    void everyRetryUsesANewKeySoThePlatformSendsTheStopToTheChargerAgain() {
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(new CommandOutcome(false, "CHARGER_REFUSED", null));
        ChargingSession session = active();

        at(START).issue(session);
        at(START.plusSeconds(15)).issue(session);

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(gateway, times(2)).stop(eq("DY5"), eq(2), keys.capture());
        assertThat(keys.getAllValues().get(0)).isNotEqualTo(keys.getAllValues().get(1));
    }

    @Test
    void twoStopsInTheSameInstantShareAKeyAndSendOnlyOneCommand() {
        when(gateway.stop(anyString(), anyInt(), anyString())).thenReturn(new CommandOutcome(true, null, null));
        ChargingSession session = active();

        at(START).issue(session);
        at(START.plusSeconds(2)).issue(session);

        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        verify(gateway, times(2)).stop(eq("DY5"), eq(2), keys.capture());
        assertThat(keys.getAllValues().get(0)).isEqualTo(keys.getAllValues().get(1));
    }
}
