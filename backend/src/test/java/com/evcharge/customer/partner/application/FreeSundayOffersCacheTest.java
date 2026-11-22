package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeSundayOffersCacheTest {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);
    private static final Instant NOW = Instant.parse("2026-11-22T14:00:00Z");

    private final NdzelaFreeCharging ndzela = mock(NdzelaFreeCharging.class);
    private final FreeSundayClaims claims = mock(FreeSundayClaims.class);
    private final LinkStore links = mock(LinkStore.class);
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final FreeSundayOffers offers = new FreeSundayOffers(ndzela, claims, links, new EligibilityCache(clock));
    private final UUID customer = UUID.randomUUID();

    @BeforeEach
    void eligible() {
        when(links.isLinked("NDZELA", customer)).thenReturn(true);
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(),
                SUNDAY, NOW.plusSeconds(7200)));
    }

    @Test
    void aSecondOfferWithinThirtySecondsMakesNoNdzelaCall() {
        offers.offer(customer);
        offers.offer(customer);
        verify(ndzela, times(1)).check(customer);
    }

    @Test
    void claimForNeverUsesTheCache() {
        offers.offer(customer);
        offers.claimFor(customer, UUID.randomUUID());
        offers.claimFor(customer, UUID.randomUUID());
        verify(ndzela, times(3)).check(customer);
    }
}
