package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeWindowStopperResilienceTest {

    private final FreeWindowStopperRig rig = new FreeWindowStopperRig();

    @Test
    void retriesWhenTheChargerCannotBeReached() {
        rig.pastWindow(rig.stored(SessionState.ACTIVE, "CP-U"));
        when(rig.gateway.stop(anyString(), anyInt(), anyString())).thenThrow(new IllegalStateException("unreachable"))
                .thenReturn(CommandOutcome.success());

        rig.stopper.stopExpired();
        rig.stopper.stopExpired();

        verify(rig.gateway, times(2)).stop(eq("CP-U"), eq(1), anyString());
    }

    @Test
    void stopsAtMostABoundedNumberPerSweepAndTheRestOnTheNext() {
        int bound = FreeWindowStopper.MAX_STOPS_PER_SWEEP;
        List<ChargingSession> due = IntStream.range(0, bound + 2)
                .mapToObj(i -> rig.stored(SessionState.ACTIVE, "CP-N" + i)).toList();
        rig.pastWindow(due.toArray(ChargingSession[]::new));
        rig.chargerAnswers(false);

        rig.stopper.stopExpired();
        verify(rig.gateway, times(bound)).stop(anyString(), anyInt(), anyString());

        rig.stopper.stopExpired();
        verify(rig.gateway).stop(eq("CP-N" + bound), eq(1), anyString());
        verify(rig.gateway).stop(eq("CP-N" + (bound + 1)), eq(1), anyString());
    }

    @Test
    void oneFailingSessionDoesNotBlockTheOthers() {
        ChargingSession broken = rig.stored(SessionState.ACTIVE, "CP-X");
        UUID unreadable = UUID.randomUUID();
        when(rig.sessions.findById(unreadable)).thenThrow(new IllegalStateException("database hiccup"));
        ChargingSession healthy = rig.stored(SessionState.ACTIVE, "CP-Y");
        rig.pastWindow(broken.id(), unreadable, healthy.id());
        when(rig.gateway.stop(eq("CP-X"), anyInt(), anyString())).thenThrow(new IllegalStateException("boom"));
        when(rig.gateway.stop(eq("CP-Y"), anyInt(), anyString())).thenReturn(CommandOutcome.success());

        rig.stopper.stopExpired();

        verify(rig.audit).record(eq("FREE_SUNDAY_WINDOW_STOPPED"), isNull(), eq(healthy.id().toString()),
                anyString());
    }
}
