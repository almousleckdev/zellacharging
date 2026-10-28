package com.evcharge.customer.wallet;

public record WalletFigures(long wallets, long balanceMinor, long reservedMinor, long topupsSucceededMinor,
                            long pendingTopups, long withdrawalsPaidMinor, long pendingWithdrawals,
                            long capturedMinor) {
}
