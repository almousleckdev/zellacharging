package com.evcharge.customer.partner.application;

import com.evcharge.customer.charging.ChargeStopper;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.stereotype.Service;

@Service
public class PartnerStopService {

    private final ChargeStopper stopper;
    private final AuditTrail audit;

    PartnerStopService(ChargeStopper stopper, AuditTrail audit) {
        this.stopper = stopper;
        this.audit = audit;
    }

    public void stop(PartnerPrincipal principal, int transactionId) {
        stopper.stopTransaction(transactionId);
        audit.record("PARTNER_SESSION_STOP_REQUESTED", principal.keyId(), String.valueOf(transactionId),
                principal.partner());
    }
}
