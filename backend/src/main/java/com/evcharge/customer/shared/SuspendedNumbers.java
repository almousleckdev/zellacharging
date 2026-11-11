package com.evcharge.customer.shared;

import java.util.UUID;

public interface SuspendedNumbers {

    boolean refuse(UUID receivingCustomerId, String msisdn, long amountMinor, String currency,
                   String idempotencyKey);
}
