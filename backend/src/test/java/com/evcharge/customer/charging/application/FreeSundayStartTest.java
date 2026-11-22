package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FreeSundayStartTest extends FreeSundayTestBase {

    @Test
    void eligibleDriverStartsWithAWaivedHoldAndAClaim() {
        UUID customer = driver(0);
        ChargingSession session = start(customer, fixtures.station(), null);

        assertThat(session.state()).isEqualTo(SessionState.START_ACCEPTED);
        assertThat(fixtures.waived(session.id())).isTrue();
        assertThat(fixtures.reserved(customer)).isZero();
        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
    }

    @Test
    void secondStartTheSameSundayIsPaid() {
        UUID customer = driver(10000);
        close(start(customer, fixtures.station(), null), SessionState.COMPLETED, new BigDecimal("4.2"));

        ChargingSession second = start(customer, fixtures.station(), null);

        assertThat(fixtures.waived(second.id())).isFalse();
        assertThat(fixtures.reserved(customer)).isEqualTo(2000);
        assertThat(fixtures.claimStates(customer)).containsExactly("CLAIMED");
    }

    @Test
    void secondStartWithNoFundsFailsWithINSUFFICIENT_FUNDS() {
        UUID customer = driver(0);
        close(start(customer, fixtures.station(), null), SessionState.COMPLETED, new BigDecimal("4.2"));

        assertThatThrownBy(() -> start(customer, fixtures.station(), null))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(fixtures.claimStates(customer)).containsExactly("CLAIMED");
    }

    @Test
    void failedStartReleasesTheClaim() {
        UUID customer = driver(0);
        ChargingSession session = start(customer, fixtures.rejectingStation(), null);

        assertThat(session.state()).isEqualTo(SessionState.FAILED);
        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("RELEASED");
        assertThat(fixtures.holdState(session.id())).isEqualTo("RELEASED");
    }

    @Test
    void sessionInsertFailureReleasesTheClaim() {
        UUID customer = driver(0);
        doThrow(new IllegalStateException("insert failed")).when(sessions)
                .insert(argThat(s -> s.customerId().equals(customer)), any());

        assertThatThrownBy(() -> start(customer, fixtures.station(), null)).isInstanceOf(IllegalStateException.class);
        assertThat(fixtures.claimStates(customer)).containsExactly("RELEASED");
    }

    @Test
    void freeStartWithAnInvalidAmountReleasesTheClaim() {
        UUID customer = driver(0);

        assertThatThrownBy(() -> service.start(customer, fixtures.station(), 1, 10, null))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("INVALID_AMOUNT");
        assertThat(fixtures.claimStates(customer)).doesNotContain("CLAIMED");
    }

    @Test
    void zeroEnergyCloseReleasesTheClaim() {
        UUID customer = driver(0);
        ChargingSession abandoned = start(customer, fixtures.station(), null);
        close(abandoned, SessionState.ABANDONED, BigDecimal.ZERO);

        assertThat(fixtures.claimStateOf(abandoned.id())).isEqualTo("RELEASED");
    }

    @Test
    void earlyStopWithEnergyKeepsTheClaim() {
        UUID customer = driver(0);
        ChargingSession session = start(customer, fixtures.station(), null);
        close(session, SessionState.COMPLETED, new BigDecimal("0.8"));

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
    }

    @Test
    void retryWithTheSameIdempotencyKeyCreatesNoSecondClaim() {
        UUID customer = driver(0);
        String station = fixtures.station();
        ChargingSession first = start(customer, station, "sunday-1");
        ChargingSession again = start(customer, station, "sunday-1");

        assertThat(again.id()).isEqualTo(first.id());
        assertThat(fixtures.claimStates(customer)).containsExactly("CLAIMED");
        verify(ndzela, times(1)).check(customer);
    }

    @Test
    void aWaivedHoldSettlesWithChargeZeroAndTheWalletIsUntouched() {
        UUID customer = driver(5000);
        ChargingSession session = start(customer, fixtures.station(), null);
        close(session, SessionState.COMPLETED, new BigDecimal("6.5"));

        assertThat(fixtures.balance(customer)).isEqualTo(5000);
        assertThat(fixtures.reserved(customer)).isZero();
        assertThat(jdbc.queryForObject("select captured_minor from funding_hold where session_id = ?", Long.class,
                session.id())).isZero();
        assertThat(fixtures.ledgerEntries(session.id())).isZero();
    }

    @Test
    void ndzelaUnavailableGivesANormalPaidStart() {
        UUID customer = fixtures.linkedCustomerWith(10000);
        ChargingSession session = start(customer, fixtures.station(), null);

        assertThat(session.state()).isEqualTo(SessionState.START_ACCEPTED);
        assertThat(fixtures.waived(session.id())).isFalse();
        assertThat(fixtures.reserved(customer)).isEqualTo(2000);
        assertThat(fixtures.claimStates(customer)).isEmpty();
    }
}
