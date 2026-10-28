package com.evcharge.customer.wallet;

import java.time.Instant;
import java.util.UUID;

public record PaymentRecord(String kind, UUID id, UUID customerId, String customerName, String operator,
                            String msisdn, long amountMinor, String currency, int decimals, String state,
                            String failureCode, String reference, Instant createdAt) {
}
