package com.evcharge.customer.wallet.domain;

import java.util.Optional;

public record LedgerDetail(
        LedgerEntry entry,
        long balanceAfterMinor,
        Optional<Topup> topup,
        Optional<Withdrawal> withdrawal) {
}
