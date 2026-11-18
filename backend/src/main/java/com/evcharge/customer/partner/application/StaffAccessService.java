package com.evcharge.customer.partner.application;

import com.evcharge.customer.account.ComplimentaryAccess;
import com.evcharge.customer.account.ComplimentaryCategory;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StaffAccessService {

    private final LinkStore links;
    private final ComplimentaryAccess complimentary;
    private final AuditTrail audit;

    StaffAccessService(LinkStore links, ComplimentaryAccess complimentary, AuditTrail audit) {
        this.links = links;
        this.complimentary = complimentary;
        this.audit = audit;
    }

    public void grant(PartnerPrincipal principal, UUID customerId, String reason) {
        if (!links.isLinked(principal.partner(), customerId)) {
            throw new ApiException(HttpStatus.CONFLICT, "CUSTOMER_NOT_LINKED", "The customer is not linked");
        }
        complimentary.grant(customerId, principal.keyId(), reason.trim(), ComplimentaryCategory.NDZELA_STAFF.name(), null);
        audit.record("PARTNER_STAFF_ACCESS_GRANTED", principal.keyId(), customerId.toString(), principal.partner());
    }

    public void revoke(PartnerPrincipal principal, UUID customerId) {
        boolean ownGrant = complimentary.find(customerId)
                .filter(grant -> ComplimentaryCategory.NDZELA_STAFF.name().equals(grant.category())).isPresent();
        if (ownGrant && complimentary.revoke(customerId)) {
            audit.record("PARTNER_STAFF_ACCESS_REVOKED", principal.keyId(), customerId.toString(), principal.partner());
        }
    }
}
