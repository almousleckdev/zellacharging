package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.OrphanChargeAdopter;
import com.evcharge.customer.charging.application.OrphanChargeGuard;
import com.evcharge.customer.charging.application.RemoteSessionLookup;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.application.port.TransactionClaims;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrphanChargeGuardTest {

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final TransactionClaims claims = mock(TransactionClaims.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final RemoteSessionLookup lookup = mock(RemoteSessionLookup.class);
    private final OrphanChargeAdopter adopter = mock(OrphanChargeAdopter.class);
    private final OrphanChargeGuard guard = new OrphanChargeGuard(gateway, lookup, adopter, claims,
            sessions);

    private RemoteSession running(int transaction, String idTag) {
        return new RemoteSession(transaction, "CP-1", 1, "ACTIVE", Instant.now().minusSeconds(60), null, null, null,
                null, null, null, 150L, idTag, null, null);
    }

    @Test
    void anUntrackedChargeThatCannotBeAdoptedIsStopped() {
        when(lookup.activeSessions()).thenReturn(List.of(running(825, "Z-A")));
        when(claims.knownIdTag("Z-A")).thenReturn(true);

        assertThat(guard.stopOrphans()).isEqualTo(1);

        verify(gateway).stop(eq("CP-1"), eq(1), anyString());
    }

    @Test
    void aChargeBoundToOneOfOurSessionsIsLeftAlone() {
        when(lookup.activeSessions()).thenReturn(List.of(running(823, "Z-A")));
        when(claims.knownIdTag("Z-A")).thenReturn(true);
        when(claims.heldByAnother(eq(823), any())).thenReturn(true);

        assertThat(guard.stopOrphans()).isZero();

        verify(gateway, never()).stop(anyString(), anyInt(), anyString());
    }

    @Test
    void aChargeWhoseSessionIsStillWaitingToBeMatchedIsLeftAlone() {
        when(lookup.activeSessions()).thenReturn(List.of(running(825, "Z-A")));
        when(claims.knownIdTag("Z-A")).thenReturn(true);
        when(sessions.findLiveAt("CP-1", 1, "Z-A")).thenReturn(Optional.of(mock(ChargingSession.class)));

        assertThat(guard.stopOrphans()).isZero();
    }

    @Test
    void aChargeOfSomeoneElseOnTheSamePlatformIsLeftAlone() {
        when(lookup.activeSessions()).thenReturn(List.of(running(900, "OTHER-OPERATOR")));
        when(claims.knownIdTag("OTHER-OPERATOR")).thenReturn(false);

        assertThat(guard.stopOrphans()).isZero();

        verify(gateway, never()).stop(anyString(), anyInt(), anyString());
    }

    @Test
    void anUntrackedChargeThatCanBeAdoptedKeepsRunning() {
        when(lookup.activeSessions()).thenReturn(List.of(running(825, "Z-A")));
        when(claims.knownIdTag("Z-A")).thenReturn(true);
        when(adopter.adopt(any())).thenReturn(true);

        assertThat(guard.stopOrphans()).isZero();

        verify(gateway, never()).stop(anyString(), anyInt(), anyString());
    }
}
