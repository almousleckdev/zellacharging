package com.evcharge.customer.partner.application.port;

import com.evcharge.customer.partner.domain.EvidenceEvent;
import com.evcharge.customer.partner.domain.EvidenceItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EvidenceStore {

    boolean record(EvidenceEvent event);

    List<EvidenceItem> after(String partner, long sequence, int size, UUID suspendedCustomerId, Instant visibleBefore);
}
