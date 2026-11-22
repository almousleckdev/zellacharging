package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.FreeSundayAccess;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartFundingTest {

    private final WalletFunding funding = mock(WalletFunding.class);
    private final FreeSundayAccess freeSunday = mock(FreeSundayAccess.class);
    private final StartFunding start = new StartFunding(funding, freeSunday);
    private final UUID customer = UUID.randomUUID();
    private final UUID session = UUID.randomUUID();

    @BeforeEach
    void limits() {
        when(funding.startLimits()).thenReturn(new StartLimits(200, 200000, "XAF", 0));
        when(freeSunday.holdsClaim(session)).thenReturn(true);
    }

    @Test
    void aClaimedStartReservesTheServerMaximumAndReturnsIt() {
        when(freeSunday.claimFor(customer, session)).thenReturn(true);

        assertThat(start.fund(customer, session, 200, () -> { })).isEqualTo(200000);

        verify(funding).reserveFree(customer, session, 200000);
    }

    @Test
    void aClaimReleasedWhileTheFreeHoldWasTakenFallsBackToAPaidReserve() {
        when(freeSunday.claimFor(customer, session)).thenReturn(true);
        when(funding.reserveFree(customer, session, 200000)).thenAnswer(invocation -> {
            when(freeSunday.holdsClaim(session)).thenReturn(false);
            return null;
        });

        assertThat(start.fund(customer, session, 2000, () -> { })).isEqualTo(2000);

        InOrder order = inOrder(funding);
        order.verify(funding).reserveFree(customer, session, 200000);
        order.verify(funding).discardWaived(session);
        order.verify(funding).reserve(customer, session, 2000);
    }

    @Test
    void aClaimStillHeldAfterTheFreeHoldKeepsTheWaivedHold() {
        when(freeSunday.claimFor(customer, session)).thenReturn(true);

        start.fund(customer, session, 2000, () -> { });

        verify(funding, never()).discardWaived(session);
        verify(funding, never()).reserve(eq(customer), eq(session), anyLong());
    }

    @Test
    void aPaidStartReservesAndReturnsTheClientAmount() {
        assertThat(start.fund(customer, session, 2000, () -> { })).isEqualTo(2000);

        verify(funding).reserve(customer, session, 2000);
    }

    @Test
    void aFailingClaimStepFallsBackToAPaidReserve() {
        when(freeSunday.claimFor(customer, session)).thenThrow(new IllegalStateException("down"));

        start.fund(customer, session, 2000, () -> { });

        verify(funding).reserve(customer, session, 2000);
        verify(funding, never()).reserveFree(eq(customer), eq(session), anyLong());
        verify(freeSunday).release(session);
    }

    @Test
    void aHoldReleaseFailureStillReleasesTheClaimAndKeepsTheInsertError() {
        when(freeSunday.claimFor(customer, session)).thenReturn(true);
        doThrow(new IllegalStateException("hold")).when(funding).release(session);
        IllegalArgumentException insert = new IllegalArgumentException("insert");

        assertThatThrownBy(() -> start.fund(customer, session, 2000, () -> {
            throw insert;
        })).isSameAs(insert);

        verify(freeSunday).release(session);
    }

    @Test
    void aClaimReleaseFailureDoesNotMaskTheReservationError() {
        ApiException invalid = new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "out of range");
        when(freeSunday.claimFor(customer, session)).thenReturn(true);
        when(funding.reserveFree(customer, session, 10)).thenThrow(invalid);
        doThrow(new IllegalStateException("claim")).when(freeSunday).release(session);

        assertThatThrownBy(() -> start.fund(customer, session, 10, () -> { })).isSameAs(invalid);
    }
}
