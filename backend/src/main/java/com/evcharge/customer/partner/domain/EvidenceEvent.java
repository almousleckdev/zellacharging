package com.evcharge.customer.partner.domain;

import java.time.Instant;
import java.util.UUID;

public record EvidenceEvent(String partner, UUID suspendedCustomerId, UUID receivingCustomerId,
                            boolean receivingLinked, String kind, String msisdn, long amountMinor,
                            String currency, String idempotencyKey, Instant occurredAt) {

    public static final String TOPUP_FROM_SUSPENDED_NUMBER = "TOPUP_FROM_SUSPENDED_NUMBER";
}
