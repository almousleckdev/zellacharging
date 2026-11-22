package com.evcharge.customer.partner.application;

import com.evcharge.customer.account.CustomerContacts;
import com.evcharge.customer.partner.PartnerLinking;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
class AdminPartnerLinking implements PartnerLinking {

    private static final String PARTNER = "NDZELA";

    private final LinkStore links;
    private final CustomerContacts contacts;
    private final AuditTrail audit;
    private final Clock clock;

    AdminPartnerLinking(LinkStore links, CustomerContacts contacts, AuditTrail audit, Clock clock) {
        this.links = links;
        this.contacts = contacts;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean linked(UUID customerId) {
        return links.isLinked(PARTNER, customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> linkedIds() {
        return new HashSet<>(links.linkedCustomerIds(PARTNER));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> linkedAmong(Collection<UUID> customerIds) {
        Set<UUID> linked = new HashSet<>(links.linkedCustomerIds(PARTNER));
        linked.retainAll(customerIds);
        return linked;
    }

    @Override
    @Transactional
    public boolean link(UUID customerId, UUID adminId) {
        if (!contacts.activeExists(customerId)) {
            throw new ApiException(HttpStatus.CONFLICT, "CUSTOMER_NOT_ACTIVE",
                    "Only an active customer can be linked");
        }
        boolean created = links.link(PARTNER, customerId, clock.instant());
        if (created) {
            audit.record("PARTNER_LINK_CREATED", adminId, customerId.toString(), PARTNER);
        }
        return created;
    }

    @Override
    @Transactional
    public boolean unlink(UUID customerId, UUID adminId) {
        boolean removed = links.unlink(PARTNER, customerId);
        if (removed) {
            audit.record("PARTNER_LINK_REMOVED", adminId, customerId.toString(), PARTNER);
        }
        return removed;
    }
}
