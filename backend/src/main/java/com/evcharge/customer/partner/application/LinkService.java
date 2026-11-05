package com.evcharge.customer.partner.application;

import com.evcharge.customer.account.CustomerContacts;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class LinkService {

    private final LinkStore links;
    private final CustomerContacts contacts;
    private final AuditTrail audit;
    private final Clock clock;

    LinkService(LinkStore links, CustomerContacts contacts, AuditTrail audit, Clock clock) {
        this.links = links;
        this.contacts = contacts;
        this.audit = audit;
        this.clock = clock;
    }

    public void link(PartnerPrincipal principal, UUID customerId) {
        if (!contacts.activeExists(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Customer not found");
        }
        if (links.link(principal.partner(), customerId, clock.instant())) {
            audit.record("PARTNER_LINK_CREATED", principal.keyId(), customerId.toString(), principal.partner());
        }
    }

    public void unlink(PartnerPrincipal principal, UUID customerId) {
        if (links.unlink(principal.partner(), customerId)) {
            audit.record("PARTNER_LINK_REMOVED", principal.keyId(), customerId.toString(), principal.partner());
        }
    }

    public List<UUID> linkedCustomerIds(PartnerPrincipal principal) {
        return links.linkedCustomerIds(principal.partner());
    }
}
