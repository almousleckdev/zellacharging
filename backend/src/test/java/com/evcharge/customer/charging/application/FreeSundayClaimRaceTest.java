package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

class FreeSundayClaimRaceTest extends FreeSundayTestBase {

    @MockitoSpyBean
    WalletFunding wallet;

    @Test
    void aClaimReleasedDuringTheFreeHoldGivesAPaidStartAndNoWaivedHold() {
        UUID customer = driver(10000);
        doAnswer(invocation -> {
            jdbc.update("update free_sunday_claim set state = 'RELEASED', released_at = now() "
                    + "where customer_id = ?", customer);
            return invocation.callRealMethod();
        }).when(wallet).reserveFree(eq(customer), any(), anyLong());

        ChargingSession session = start(customer, fixtures.station(), null);

        assertThat(fixtures.waived(session.id())).isFalse();
        assertThat(fixtures.holdState(session.id())).isEqualTo("OPEN");
        assertThat(fixtures.reserved(customer)).isEqualTo(2000);
        assertThat(fixtures.waivedHolds(customer)).isZero();
        assertThat(fixtures.claimStates(customer)).containsExactly("RELEASED");
    }
}
