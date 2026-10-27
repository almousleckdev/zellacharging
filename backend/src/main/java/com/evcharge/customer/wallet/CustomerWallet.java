package com.evcharge.customer.wallet;

public record CustomerWallet(String currency, int decimals, long balanceMinor, long reservedMinor,
                             long topupsSucceededMinor, long pendingTopups, long withdrawalsPaidMinor) {
}
