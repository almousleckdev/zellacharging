package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.shared.FreeSundayAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static com.evcharge.customer.charging.application.FreeSundayFixtures.eligible;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FreeSundaySharedAgreementTest extends FreeSundayTestBase {

    @Autowired
    FreeSundayAccess freeSunday;

    private UUID first;
    private UUID second;

    @BeforeEach
    void twoLinkedCustomersOfOneDriver() {
        FreeChargingEligibility shared = eligible();
        first = fixtures.linkedCustomerWith(10000);
        second = fixtures.linkedCustomerWith(10000);
        when(ndzela.check(first)).thenReturn(shared);
        when(ndzela.check(second)).thenReturn(shared);
    }

    @Test
    void theFirstStartIsFreeAndTheSecondCustomerPaysTheSameSunday() {
        ChargingSession free = start(first, fixtures.station(), null);
        ChargingSession paid = start(second, fixtures.station(), null);

        assertThat(fixtures.waived(free.id())).isTrue();
        assertThat(fixtures.waived(paid.id())).isFalse();
        assertThat(fixtures.reserved(second)).isEqualTo(2000);
        assertThat(fixtures.claimStates(second)).isEmpty();
    }

    @Test
    void theSecondCustomerIsClaimedOutOnceTheAgreementIsUsed() {
        assertThat(freeSunday.claimFor(first, UUID.randomUUID())).isTrue();

        assertThat(freeSunday.claimFor(second, UUID.randomUUID())).isFalse();
        assertThat(freeSunday.offer(second).available()).isFalse();
        assertThat(freeSunday.offer(second).reason()).isEqualTo("SUNDAY_USED");
    }

    @Test
    void aReleasedClaimGivesTheSundayBackToTheOtherCustomer() {
        ChargingSession abandoned = start(first, fixtures.station(), null);
        close(abandoned, SessionState.ABANDONED, BigDecimal.ZERO);

        assertThat(freeSunday.offer(second).available()).isTrue();
        ChargingSession free = start(second, fixtures.station(), null);
        assertThat(fixtures.waived(free.id())).isTrue();
    }
}
