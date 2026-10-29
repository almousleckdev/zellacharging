package com.evcharge.customer.wallet;

import java.time.Instant;

public record PaymentDetail(PaymentRecord record, String providerRef, Instant updatedAt) {
}
