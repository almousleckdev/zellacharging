package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.shared.FreeSundayOffer;
import com.evcharge.customer.shared.FreeSundayWindow;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FreeSundayOffersTest {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 11, 22);
    private static final Instant WINDOW_END = Instant.parse("2026-11-22T21:00:00Z");

    private final NdzelaFreeCharging ndzela = mock(NdzelaFreeCharging.class);
    private final FreeSundayClaims claims = mock(FreeSundayClaims.class);
    private final LinkStore links = mock(LinkStore.class);
    private final FreeSundayOffers offers = new FreeSundayOffers(ndzela, claims, links,
            new EligibilityCache(Clock.systemUTC()));
    private final UUID customer = UUID.randomUUID();
    private final UUID session = UUID.randomUUID();
    private final UUID agreement = UUID.randomUUID();

    @BeforeEach
    void ndzelaSaysEligible() {
        when(links.isLinked("NDZELA", customer)).thenReturn(true);
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", agreement,
                SUNDAY, WINDOW_END));
    }

    @Test
    void eligibleAnswerBecomesAnAvailableOffer() {
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isTrue();
        assertThat(offer.reason()).isEqualTo("ELIGIBLE");
        assertThat(offer.endsAt()).isEqualTo(WINDOW_END);
    }

    @Test
    void anAvailableOfferCarriesNdzelasWindow() {
        FreeSundayWindow window = new FreeSundayWindow("SUNDAY", "00:00", "23:59", true);
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", agreement, SUNDAY, WINDOW_END, window));

        assertThat(offers.offer(customer).window()).isEqualTo(window);
    }

    @Test
    void aRefusedOfferAlsoCarriesTheWindowSoTheAppCanSayWhenItOpens() {
        FreeSundayWindow window = new FreeSundayWindow("SUNDAY", "12:00", "22:00", false);
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(false, "OUTSIDE_WINDOW", null, null, null, window));

        FreeSundayOffer offer = offers.offer(customer);

        assertThat(offer.available()).isFalse();
        assertThat(offer.window()).isEqualTo(window);
    }

    @Test
    void anUnlinkedCustomerGetsNoWindow() {
        when(links.isLinked("NDZELA", customer)).thenReturn(false);
        assertThat(offers.offer(customer).window()).isNull();
    }

    @Test
    void notEligibleAnswerKeepsNdzelasReasonAndNoEndTime() {
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(false, "UNPAID_DAYS", null, null, null));
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isFalse();
        assertThat(offer.reason()).isEqualTo("UNPAID_DAYS");
        assertThat(offer.endsAt()).isNull();
    }

    @Test
    void unavailableNdzelaGivesUNAVAILABLEAndNoOffer() {
        when(ndzela.check(customer)).thenReturn(FreeChargingEligibility.unavailable());
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isFalse();
        assertThat(offer.reason()).isEqualTo("UNAVAILABLE");
    }

    @Test
    void offerReportsSUNDAY_USEDWhenTheSundayIsAlreadyClaimed() {
        when(claims.isClaimed(agreement, customer, SUNDAY)).thenReturn(true);
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isFalse();
        assertThat(offer.reason()).isEqualTo("SUNDAY_USED");
    }

    @Test
    void offerNeverWritesAClaim() {
        offers.offer(customer);
        verify(claims, never()).claim(any(), any(), any(), any(), any());
    }

    @Test
    void claimForTakesTheClaimOnlyWhenEligible() {
        when(claims.claim(customer, SUNDAY, session, agreement, WINDOW_END)).thenReturn(true);
        assertThat(offers.claimFor(customer, session)).isTrue();
        verify(claims).claim(customer, SUNDAY, session, agreement, WINDOW_END);
    }

    @Test
    void claimForReturnsFalseWhenSundayAlreadyClaimed() {
        when(claims.claim(customer, SUNDAY, session, agreement, WINDOW_END)).thenReturn(false);
        assertThat(offers.claimFor(customer, session)).isFalse();
    }

    @Test
    void claimForNeverClaimsWhenNdzelaSaysNotEligible() {
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(false, "OUTSIDE_WINDOW", null, null, null));
        assertThat(offers.claimFor(customer, session)).isFalse();
        verify(claims, never()).claim(any(), any(), any(), any(), any());
    }

    @Test
    void claimForNeverClaimsWhenNdzelaIsUnavailable() {
        when(ndzela.check(customer)).thenReturn(FreeChargingEligibility.unavailable());
        assertThat(offers.claimFor(customer, session)).isFalse();
        verify(claims, never()).claim(any(), any(), any(), any(), any());
    }

    @Test
    void releaseFreesTheClaimOfThatSession() {
        offers.release(session);
        verify(claims).release(session);
    }

    @Test
    void anUnlinkedCustomerGetsNOT_LINKEDWithoutAnyNdzelaCall() {
        when(links.isLinked("NDZELA", customer)).thenReturn(false);
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isFalse();
        assertThat(offer.reason()).isEqualTo("NOT_LINKED");
        assertThat(offer.endsAt()).isNull();
        verifyNoInteractions(ndzela);
    }

    @Test
    void claimForAnUnlinkedCustomerIsFalseWithoutAnyNdzelaCall() {
        when(links.isLinked("NDZELA", customer)).thenReturn(false);
        assertThat(offers.claimFor(customer, session)).isFalse();
        verifyNoInteractions(ndzela);
        verify(claims, never()).claim(any(), any(), any(), any(), any());
    }

    @Test
    void aFailingClaimLookupGivesUNAVAILABLEInsteadOfAnError() {
        when(claims.isClaimed(agreement, customer, SUNDAY)).thenThrow(new IllegalStateException("db down"));
        FreeSundayOffer offer = offers.offer(customer);
        assertThat(offer.available()).isFalse();
        assertThat(offer.reason()).isEqualTo("UNAVAILABLE");
        assertThat(offer.endsAt()).isNull();
    }
}
