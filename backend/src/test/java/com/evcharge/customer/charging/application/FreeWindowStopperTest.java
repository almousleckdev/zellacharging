package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static com.evcharge.customer.charging.application.FreeWindowStopperRig.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FreeWindowStopperTest {

    private final FreeWindowStopperRig rig = new FreeWindowStopperRig();

    @Test
    void stopsAClaimedSessionAfterTheWindowEnds() {
        ChargingSession session = rig.stored(SessionState.ACTIVE, "CP-A");
        rig.pastWindow(session);
        rig.chargerAnswers(true);

        rig.stopper.stopExpired();

        verify(rig.gateway).stop(eq("CP-A"), eq(1), anyString());
        ArgumentCaptor<ChargingSession> saved = ArgumentCaptor.forClass(ChargingSession.class);
        verify(rig.sessions).update(saved.capture());
        assertThat(saved.getValue().state()).isEqualTo(SessionState.STOPPING);
        assertThat(saved.getValue().stopReason()).isEqualTo("FREE_WINDOW_ENDED");
        verify(rig.audit).record(eq("FREE_SUNDAY_WINDOW_STOPPED"), isNull(), eq(session.id().toString()),
                anyString());
        verify(rig.announcements).freeWindowEnded(any(ChargingSession.class));
    }

    @Test
    void theDriverIsToldOnlyWhenTheStopWasAccepted() {
        ChargingSession session = rig.stored(SessionState.ACTIVE, "CP-N");
        rig.pastWindow(session);
        rig.chargerAnswers(false);

        rig.stopper.stopExpired();

        verify(rig.announcements, never()).freeWindowEnded(any());
    }

    @Test
    void looksBackOnlyOverTheLastDayOfWindows() {
        rig.pastWindow(new UUID[0]);

        rig.stopper.stopExpired();

        verify(rig.windows).sessionsPastWindow(NOW.minus(FreeWindowStopper.LOOK_BACK), NOW);
    }

    @Test
    void ignoresAlreadyStoppedOrStoppingSessions() {
        rig.pastWindow(rig.stored(SessionState.STOPPING, "CP-B"), rig.stored(SessionState.COMPLETED, "CP-C"),
                rig.stored(SessionState.ABANDONED, "CP-D"), rig.stored(SessionState.FAILED, "CP-E"));

        rig.stopper.stopExpired();

        verify(rig.gateway, never()).stop(anyString(), anyInt(), anyString());
        verify(rig.audit, never()).record(anyString(), any(), anyString(), any());
    }

    @Test
    void leavesASessionStillWaitingForThePlugUntilItCharges() {
        rig.pastWindow(rig.stored(SessionState.START_ACCEPTED, "CP-W"), rig.stored(SessionState.REQUESTED, "CP-R"));

        rig.stopper.stopExpired();

        verify(rig.gateway, never()).stop(anyString(), anyInt(), anyString());
    }

    @Test
    void retriesWhenTheChargerRefuses() {
        ChargingSession session = rig.stored(SessionState.ACTIVE, "CP-F");
        rig.pastWindow(session);
        rig.chargerAnswers(false);

        rig.stopper.stopExpired();
        verify(rig.sessions, never()).update(any());
        verify(rig.audit, never()).record(anyString(), any(), anyString(), any());

        rig.chargerAnswers(true);
        rig.stopper.stopExpired();

        verify(rig.gateway, times(2)).stop(eq("CP-F"), eq(1), anyString());
        verify(rig.audit).record(eq("FREE_SUNDAY_WINDOW_STOPPED"), isNull(), eq(session.id().toString()),
                anyString());
    }
}
