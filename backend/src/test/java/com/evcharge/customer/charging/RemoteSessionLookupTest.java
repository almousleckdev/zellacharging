package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.RemoteSessionLookup;
import com.evcharge.customer.charging.application.SessionSettings;
import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.application.port.StartAttempts;
import com.evcharge.customer.charging.application.port.TransactionClaims;
import com.evcharge.customer.charging.domain.ChargingSession;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteSessionLookupTest {

    private static final Instant NOW = Instant.parse("2026-10-16T12:00:00Z");

    private final ChargingGateway gateway = mock(ChargingGateway.class);
    private final TransactionClaims claims = mock(TransactionClaims.class);
    private final StartAttempts attempts = mock(StartAttempts.class);
    private final SessionSettings settings = new SessionSettings(Duration.ofSeconds(60), Duration.ofSeconds(15),
            Duration.ofSeconds(30), 150L, 95, Duration.ofSeconds(120), Duration.ofSeconds(5),
            Duration.ofSeconds(30));
    private final RemoteSessionLookup lookup = new RemoteSessionLookup(gateway, claims, attempts, settings,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private ChargingSession local(String station, String idTag, Integer transaction, SessionState state) {
        return new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), station, 1, idTag, state, null, transaction,
                null, null, null, null, null, null, null, NOW.minusSeconds(30), NOW);
    }

    private RemoteSession remote(int transaction, String station, String idTag, String status) {
        return new RemoteSession(transaction, station, 1, status, NOW.minusSeconds(20), null, null, null, null, null,
                null, 150L, idTag, null, null);
    }

    @Test
    void manyLiveSessionsAreResolvedFromOneListCall() {
        when(gateway.activeSessions()).thenReturn(List.of(remote(1, "CP-1", "Z-A", "ACTIVE"),
                remote(2, "CP-2", "Z-B", "ACTIVE"), remote(3, "CP-3", "Z-C", "ACTIVE")));
        ChargingSession a = local("CP-1", "Z-A", 1, SessionState.ACTIVE);
        ChargingSession b = local("CP-2", "Z-B", 2, SessionState.ACTIVE);
        ChargingSession c = local("CP-3", "Z-C", null, SessionState.START_ACCEPTED);

        assertThat(lookup.find(a)).map(RemoteSession::transactionId).contains(1);
        assertThat(lookup.find(b)).map(RemoteSession::transactionId).contains(2);
        assertThat(lookup.find(c)).map(RemoteSession::transactionId).contains(3);

        verify(gateway, times(1)).activeSessions();
        verify(gateway, never()).sessionsOf(anyString());
        verify(gateway, never()).session(anyInt());
    }

    @Test
    void aSessionThatLeftTheActiveListIsLookedUpOnceByItsTransaction() {
        when(gateway.activeSessions()).thenReturn(List.of());
        when(gateway.session(7)).thenReturn(Optional.of(remote(7, "CP-1", "Z-A", "COMPLETED")));

        Optional<RemoteSession> found = lookup.find(local("CP-1", "Z-A", 7, SessionState.ACTIVE));

        assertThat(found).map(RemoteSession::status).contains("COMPLETED");
        verify(gateway, never()).sessionsOf(anyString());
    }

    @Test
    void anotherCustomersSessionOnTheSameConnectorIsNeverApplied() {
        when(gateway.activeSessions()).thenReturn(List.of(remote(9, "CP-1", "Z-OTHER", "ACTIVE")));
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of());

        assertThat(lookup.find(local("CP-1", "Z-A", null, SessionState.START_ACCEPTED))).isEmpty();
    }

    @Test
    void aWaitingSessionIsNotQueriedByIdTagOnEveryPass() {
        when(gateway.activeSessions()).thenReturn(List.of());
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of());
        ChargingSession waiting = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-A",
                SessionState.START_ACCEPTED, null, null, null, null, null, null, null, null, null, NOW.minusSeconds(5),
                NOW);

        lookup.find(waiting);
        lookup.find(waiting);
        lookup.find(waiting);

        verify(gateway, times(1)).sessionsOf("Z-A");
    }

    @Test
    void aSessionAboutToTimeOutIsAlwaysCheckedByIdTagFirst() {
        when(gateway.activeSessions()).thenReturn(List.of());
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of());
        ChargingSession nearlyExpired = new ChargingSession(UUID.randomUUID(), UUID.randomUUID(), "CP-1", 1, "Z-A",
                SessionState.START_ACCEPTED, null, null, null, null, null, null, null, null, null,
                NOW.minusSeconds(55), NOW);

        lookup.find(nearlyExpired);
        lookup.find(nearlyExpired);

        verify(gateway, times(2)).sessionsOf("Z-A");
    }

    private RemoteSession remoteStartedAt(Instant startedAt, String status) {
        return new RemoteSession(11, "CP-1", 1, status, startedAt, null, null, null, null, null, null, 150L, "Z-A",
                null, null);
    }

    @Test
    void anActiveSessionIsFoundEvenWhenTheStationClockIsFarBehind() {
        when(gateway.activeSessions()).thenReturn(List.of(remoteStartedAt(NOW.minus(Duration.ofHours(5)), "ACTIVE")));

        Optional<RemoteSession> found = lookup.find(local("CP-1", "Z-A", null, SessionState.START_ACCEPTED));

        assertThat(found).map(RemoteSession::transactionId).contains(11);
    }

    @Test
    void aFinishedOldSessionIsNeverTakenForTheNewCharge() {
        when(gateway.activeSessions()).thenReturn(List.of());
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of(remoteStartedAt(NOW.minus(Duration.ofHours(5)),
                "COMPLETED")));

        assertThat(lookup.find(local("CP-1", "Z-A", null, SessionState.START_ACCEPTED))).isEmpty();
    }

    @Test
    void aTransactionAlreadyHeldByAnotherSessionIsNeverAdopted() {
        when(gateway.activeSessions()).thenReturn(List.of());
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of(remoteStartedAt(NOW.minusSeconds(40), "COMPLETED")));
        when(claims.heldByAnother(eq(11), any())).thenReturn(true);

        assertThat(lookup.find(local("CP-1", "Z-A", null, SessionState.START_ACCEPTED))).isEmpty();
    }

    @Test
    void aRunningTransactionHeldByAnotherSessionIsNeverAdopted() {
        when(gateway.activeSessions()).thenReturn(List.of(remoteStartedAt(NOW.minusSeconds(40), "ACTIVE")));
        when(gateway.sessionsOf("Z-A")).thenReturn(List.of());
        when(claims.heldByAnother(eq(11), any())).thenReturn(true);

        assertThat(lookup.find(local("CP-1", "Z-A", null, SessionState.START_ACCEPTED))).isEmpty();
    }
}
