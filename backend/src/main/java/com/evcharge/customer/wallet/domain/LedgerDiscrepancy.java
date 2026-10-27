package com.evcharge.customer.wallet.domain;

import java.util.UUID;

public record LedgerDiscrepancy(UUID customerId, long balanceMinor, long expectedBalanceMinor, long reservedMinor,
                                long expectedReservedMinor) {
}
