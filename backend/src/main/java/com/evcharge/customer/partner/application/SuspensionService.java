package com.evcharge.customer.partner.application;

import com.evcharge.customer.account.CustomerModeration;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.partner.application.port.SuspensionStore;
import com.evcharge.customer.partner.domain.ActiveSuspension;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SuspensionService {

    private final LinkStore links;
    private final SuspensionStore suspensions;
    private final CustomerModeration moderation;
    private final AuditTrail audit;
    private final Notifier notifier;
    private final Clock clock;

    SuspensionService(LinkStore links, SuspensionStore suspensions, CustomerModeration moderation,
                      AuditTrail audit, Notifier notifier, Clock clock) {
        this.links = links;
        this.suspensions = suspensions;
        this.moderation = moderation;
        this.audit = audit;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Transactional
    public void suspend(PartnerPrincipal principal, UUID customerId, long owedMinor, String currency, int decimals) {
        if (!links.isLinked(principal.partner(), customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Customer not found");
        }
        boolean blockedBefore = moderation.suspended(customerId);
        if (!blockedBefore) {
            moderation.suspend(customerId);
        }
        boolean fresh = suspensions.active(principal.partner(), customerId).isEmpty();
        suspensions.suspend(principal.partner(), customerId, owedMinor, currency, decimals, blockedBefore,
                suspensions.knownNumbers(customerId), clock.instant());
        if (fresh) {
            notifier.notify(customerId, NotificationType.ACCOUNT_SUSPENDED_ARREARS,
                    "suspension:" + customerId + ":" + clock.instant().toEpochMilli(),
                    Map.of("owedMinor", String.valueOf(owedMinor)));
        }
        audit.record("PARTNER_SUSPENSION_APPLIED", principal.keyId(), customerId.toString(), principal.partner());
    }

    @Transactional
    public void restore(PartnerPrincipal principal, UUID customerId) {
        Optional<ActiveSuspension> active = suspensions.active(principal.partner(), customerId);
        if (active.isEmpty()) {
            return;
        }
        suspensions.lift(principal.partner(), customerId, clock.instant());
        if (!active.get().blockedBefore() && moderation.suspended(customerId)) {
            moderation.reactivate(customerId);
        }
        notifier.notify(customerId, NotificationType.ACCOUNT_RESTORED,
                "restored:" + customerId + ":" + clock.instant().toEpochMilli(), Map.of());
        audit.record("PARTNER_SUSPENSION_LIFTED", principal.keyId(), customerId.toString(), principal.partner());
    }
}
