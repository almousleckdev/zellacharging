package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.application.port.SuspensionStore;
import com.evcharge.customer.partner.domain.ActiveSuspension;
import com.evcharge.customer.shared.SuspensionNotice;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;
import java.util.UUID;

@Component
class PartnerSuspensionNotice implements SuspensionNotice {

    private static final String PARTNER = "NDZELA";

    private final SuspensionStore suspensions;

    PartnerSuspensionNotice(SuspensionStore suspensions) {
        this.suspensions = suspensions;
    }

    @Override
    public OptionalLong owedMinor(UUID customerId) {
        return suspensions.active(PARTNER, customerId).map(ActiveSuspension::owedMinor)
                .map(OptionalLong::of).orElse(OptionalLong.empty());
    }
}
