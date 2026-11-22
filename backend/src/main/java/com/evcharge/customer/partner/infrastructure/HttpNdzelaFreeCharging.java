package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class HttpNdzelaFreeCharging implements NdzelaFreeCharging {

    private static final Logger log = LoggerFactory.getLogger(HttpNdzelaFreeCharging.class);

    private static final String ELIGIBLE = "ELIGIBLE";

    private final NdzelaRestClient ndzela;

    HttpNdzelaFreeCharging(NdzelaRestClient ndzela) {
        this.ndzela = ndzela;
    }

    @Override
    public FreeChargingEligibility check(UUID customerId) {
        try {
            FreeChargingEligibility answer = ndzela.getForCustomer("/free-charging", customerId)
                    .body(FreeChargingEligibility.class);
            if (answer == null || answer.reason() == null || (answer.eligible() && !usableGrant(answer))) {
                log.warn("NDZela gave an unusable free-charging answer");
                return FreeChargingEligibility.unavailable();
            }
            return answer;
        } catch (RuntimeException e) {
            log.warn("NDZela free-charging check failed: {}", NdzelaFailure.describe(e));
            return FreeChargingEligibility.unavailable();
        }
    }

    private static boolean usableGrant(FreeChargingEligibility answer) {
        return ELIGIBLE.equals(answer.reason()) && answer.complete();
    }
}
