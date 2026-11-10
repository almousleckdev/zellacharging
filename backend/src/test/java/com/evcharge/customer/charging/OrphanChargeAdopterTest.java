package com.evcharge.customer.charging;

import com.evcharge.customer.charging.application.OrphanChargeAdopter;
import com.evcharge.customer.charging.application.SessionSettlement;
import com.evcharge.customer.charging.application.port.IdTagRepository;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrphanChargeAdopterTest {

    private static final Instant NOW = Instant.parse("2026-11-10T08:10:00Z");
    private static final UUID CUSTOMER = UUID.randomUUID();

    private final IdTagRepository idTags = mock(IdTagRepository.class);
    private final SessionRepository sessions = mock(SessionRepository.class);
    private final WalletFunding funding = mock(WalletFunding.class);
    private final SessionSettlement settlement = mock(SessionSettlement.class);
    private final OrphanChargeAdopter adopter = new OrphanChargeAdopter(idTags, sessions, funding, settlement,
            Clock.fixed(NOW, ZoneOffset.UTC));
    private final RemoteSession remote = new RemoteSession(825, "CP-1", 1, "ACTIVE", NOW.minusSeconds(60), null, null,
            null, null, null, null, 150L, "Z-A", null, null);

    private void customerWithFunds(long available) {
        when(idTags.customerOf("Z-A")).thenReturn(Optional.of(CUSTOMER));
        when(sessions.findLive(CUSTOMER)).thenReturn(Optional.empty());
        when(funding.available(CUSTOMER)).thenReturn(new Money(available, "XAF", 0));
        when(funding.startLimits()).thenReturn(new StartLimits(500, 10000, "XAF", 0));
    }

    @Test
    void anUntrackedChargeBecomesAnActiveSessionFundedUpToTheStartMaximum() {
        customerWithFunds(25000);

        assertThat(adopter.adopt(remote)).isTrue();

        verify(funding).reserve(eq(CUSTOMER), any(), eq(10000L));
        verify(sessions).insert(any(), eq("adopt:825"));
        ArgumentCaptor<ChargingSession> saved = ArgumentCaptor.forClass(ChargingSession.class);
        verify(sessions).update(saved.capture());
        assertThat(saved.getValue().state()).isEqualTo(SessionState.ACTIVE);
        assertThat(saved.getValue().transactionId()).isEqualTo(825);
        assertThat(saved.getValue().customerId()).isEqualTo(CUSTOMER);
        verify(settlement).onChange(any(), eq(saved.getValue()));
    }

    @Test
    void aCustomerWithTooLittleMoneyIsNotAdopted() {
        customerWithFunds(200);

        assertThat(adopter.adopt(remote)).isFalse();

        verify(funding, never()).reserve(any(), any(), anyLong());
        verify(sessions, never()).insert(any(), any());
    }

    @Test
    void aCustomerWhoAlreadyHasALiveChargeIsNotAdopted() {
        customerWithFunds(25000);
        when(sessions.findLive(CUSTOMER)).thenReturn(Optional.of(mock(ChargingSession.class)));

        assertThat(adopter.adopt(remote)).isFalse();

        verify(sessions, never()).insert(any(), any());
    }

    @Test
    void anUnknownIdTagIsNotAdopted() {
        when(idTags.customerOf("Z-A")).thenReturn(Optional.empty());

        assertThat(adopter.adopt(remote)).isFalse();
    }

    @Test
    void theHoldIsReleasedWhenTheSessionCannotBeSaved() {
        customerWithFunds(25000);
        doThrow(new IllegalStateException("duplicate")).when(sessions).insert(any(), any());

        assertThat(adopter.adopt(remote)).isFalse();

        verify(funding).release(any());
    }
}
