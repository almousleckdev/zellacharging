package com.evcharge.customer.wallet.domain;

public record WalletBalance(long balanceMinor, long reservedMinor, String currency, int decimals) {

    public long availableMinor() {
        return balanceMinor - reservedMinor;
    }
}
