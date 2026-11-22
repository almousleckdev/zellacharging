package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.started;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FreeSundayReleaseTest extends FreeSundayTestBase {

    @Autowired
    StalledSessionCloser closer;

    private ChargingSession freeSession(UUID customer) {
        return start(customer, fixtures.station(), null);
    }

    @Test
    void startedAbandonedWithUnknownEnergyKeepsTheClaim() {
        ChargingSession session = freeSession(driver(0));
        close(session, SessionState.ABANDONED, null);

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
    }

    @Test
    void lostChargerWithNoMeterValuesKeepsTheClaim() {
        ChargingSession session = started(freeSession(driver(0)));
        sessions.update(session);

        closer.closeLostCharger(session);

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
    }

    @Test
    void neverStartedTimeoutReleasesTheClaim() {
        ChargingSession session = freeSession(driver(0));
        ChargingSession timedOut = session.withState(SessionState.FAILED, "START_TIMEOUT", Instant.now());
        sessions.update(timedOut);
        settlement.settle(timedOut);

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("RELEASED");
    }

    @Test
    void exactlyZeroEnergyOnCompletedReleasesTheClaim() {
        ChargingSession session = freeSession(driver(0));
        close(session, SessionState.COMPLETED, new BigDecimal("0.0"));

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("RELEASED");
    }

    @Test
    void deliveredEnergyKeepsTheClaim() {
        ChargingSession session = freeSession(driver(0));
        close(session, SessionState.COMPLETED, new BigDecimal("0.8"));

        assertThat(fixtures.claimStateOf(session.id())).isEqualTo("CLAIMED");
    }

    @Test
    void unknownEnergyCloseGivesNoSecondFreeSessionThatSunday() {
        UUID customer = driver(10000);
        close(freeSession(customer), SessionState.ABANDONED, null);

        ChargingSession second = freeSession(customer);

        assertThat(fixtures.waived(second.id())).isFalse();
        assertThat(fixtures.reserved(customer)).isEqualTo(2000);
        assertThat(fixtures.claimStates(customer)).containsExactly("CLAIMED");
    }

    @Test
    void aFailingClaimStepGivesAnOrdinaryPaidStart() {
        UUID customer = fixtures.linkedCustomerWith(10000);
        when(ndzela.check(customer)).thenThrow(new IllegalStateException("claim port down"));

        ChargingSession session = freeSession(customer);

        assertThat(session.state()).isEqualTo(SessionState.START_ACCEPTED);
        assertThat(fixtures.waived(session.id())).isFalse();
        assertThat(fixtures.reserved(customer)).isEqualTo(2000);
        assertThat(fixtures.claimStates(customer)).isEmpty();
    }
}
