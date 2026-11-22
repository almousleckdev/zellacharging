package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.evcharge.customer.charging.application.FreeWindowStopperRig.NOW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeWindowStopperFreshnessTest {

    private final FreeWindowStopperRig rig = new FreeWindowStopperRig();

    @Test
    void skipsASessionThatCompletedAfterItWasRead() {
        ChargingSession active = rig.stored(SessionState.ACTIVE, "CP-S");
        ChargingSession completed = active.withState(SessionState.COMPLETED, null, NOW);
        when(rig.sessions.findById(active.id())).thenReturn(Optional.of(active), Optional.of(completed));
        rig.pastWindow(active);
        rig.chargerAnswers(true);

        rig.stopper.stopExpired();

        verify(rig.gateway, never()).stop(anyString(), anyInt(), anyString());
        verify(rig.sessions, never()).update(any());
    }

    @Test
    void skipsASessionWhoseHoldClosedAfterItWasRead() {
        ChargingSession active = rig.stored(SessionState.ACTIVE, "CP-H");
        rig.openHolds.remove(active.id());
        rig.pastWindow(active);
        rig.chargerAnswers(true);

        rig.stopper.stopExpired();

        verify(rig.gateway, never()).stop(anyString(), anyInt(), anyString());
    }

    @Test
    void neverStopsASessionWhoseHoldIsNotWaived() {
        ChargingSession paid = rig.stored(SessionState.ACTIVE, "CP-P");
        when(rig.funding.waived(paid.id())).thenReturn(false);
        rig.pastWindow(paid);
        rig.chargerAnswers(true);

        rig.stopper.stopExpired();

        verify(rig.gateway, never()).stop(anyString(), anyInt(), anyString());
    }
}
