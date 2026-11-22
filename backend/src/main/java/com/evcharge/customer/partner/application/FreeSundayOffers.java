package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import com.evcharge.customer.shared.FreeSundayAccess;
import com.evcharge.customer.shared.FreeSundayOffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class FreeSundayOffers implements FreeSundayAccess {

    static final String SUNDAY_USED = "SUNDAY_USED";
    static final String NOT_LINKED = "NOT_LINKED";

    private static final String PARTNER = "NDZELA";
    private static final Logger log = LoggerFactory.getLogger(FreeSundayOffers.class);

    private final NdzelaFreeCharging ndzela;
    private final FreeSundayClaims claims;
    private final LinkStore links;
    private final EligibilityCache cache;

    FreeSundayOffers(NdzelaFreeCharging ndzela, FreeSundayClaims claims, LinkStore links, EligibilityCache cache) {
        this.ndzela = ndzela;
        this.claims = claims;
        this.links = links;
        this.cache = cache;
    }

    @Override
    public FreeSundayOffer offer(UUID customerId) {
        if (!links.isLinked(PARTNER, customerId)) {
            return new FreeSundayOffer(false, NOT_LINKED, null);
        }
        FreeChargingEligibility answer = cache.get(customerId, ndzela::check);
        if (!answer.eligible()) {
            return new FreeSundayOffer(false, answer.reason(), null, answer.window());
        }
        try {
            if (claims.isClaimed(answer.agreementId(), customerId, answer.sundayDate())) {
                return new FreeSundayOffer(false, SUNDAY_USED, null, answer.window());
            }
        } catch (RuntimeException e) {
            log.warn("Free Sunday claim lookup failed: {}", e.getClass().getSimpleName());
            return new FreeSundayOffer(false, FreeChargingEligibility.UNAVAILABLE, null);
        }
        return new FreeSundayOffer(true, answer.reason(), answer.windowEndsAt(), answer.window());
    }

    @Override
    public boolean claimFor(UUID customerId, UUID sessionId) {
        if (!links.isLinked(PARTNER, customerId)) {
            return false;
        }
        FreeChargingEligibility answer = ndzela.check(customerId);
        return answer.eligible()
                && claims.claim(customerId, answer.sundayDate(), sessionId, answer.agreementId(),
                answer.windowEndsAt());
    }

    @Override
    public boolean holdsClaim(UUID sessionId) {
        return claims.agreementOf(sessionId).isPresent();
    }

    @Override
    public void release(UUID sessionId) {
        claims.release(sessionId);
    }
}
