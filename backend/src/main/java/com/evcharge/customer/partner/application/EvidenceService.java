package com.evcharge.customer.partner.application;

import com.evcharge.customer.partner.application.port.EvidenceStore;
import com.evcharge.customer.partner.application.port.LinkStore;
import com.evcharge.customer.partner.application.port.SuspensionStore;
import com.evcharge.customer.partner.domain.EvidenceEvent;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import com.evcharge.customer.shared.SuspendedNumbers;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class EvidenceService implements SuspendedNumbers {

    private static final String PARTNER = "NDZELA";

    private final SuspensionStore suspensions;
    private final EvidenceStore evidence;
    private final LinkStore links;
    private final Notifier notifier;
    private final Clock clock;

    EvidenceService(SuspensionStore suspensions, EvidenceStore evidence, LinkStore links, Notifier notifier, Clock clock) {
        this.suspensions = suspensions;
        this.evidence = evidence;
        this.links = links;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public boolean refuse(UUID receivingCustomerId, String msisdn, long amountMinor, String currency,
                          String idempotencyKey) {
        List<UUID> suspended = suspensions.customersWithNumber(PARTNER, msisdn).stream()
                .filter(customer -> !customer.equals(receivingCustomerId)).toList();
        boolean linked = links.isLinked(PARTNER, receivingCustomerId);
        for (UUID customer : suspended) {
            boolean fresh = evidence.record(new EvidenceEvent(PARTNER, customer, receivingCustomerId, linked,
                    EvidenceEvent.TOPUP_FROM_SUSPENDED_NUMBER, msisdn, amountMinor, currency, idempotencyKey,
                    clock.instant()));
            if (fresh) {
                warn(customer, receivingCustomerId, linked, idempotencyKey);
            }
        }
        return !suspended.isEmpty();
    }

    private void warn(UUID suspended, UUID receiving, boolean receivingLinked, String idempotencyKey) {
        String key = suspended + ":" + receiving + ":" + idempotencyKey;
        notifier.notify(suspended, NotificationType.SUSPENDED_HELP_DETECTED, "help:" + key, Map.of());
        if (receivingLinked) {
            notifier.notify(receiving, NotificationType.HELPER_WARNED, "helper:" + key, Map.of());
        }
    }
}
