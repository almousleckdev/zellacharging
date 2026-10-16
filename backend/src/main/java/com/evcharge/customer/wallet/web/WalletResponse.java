package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.WalletBalance;

record WalletResponse(long balanceMinor, long reservedMinor, long availableMinor, String currency, int decimals) {

    static WalletResponse from(WalletBalance wallet) {
        return new WalletResponse(wallet.balanceMinor(), wallet.reservedMinor(), wallet.availableMinor(),
                wallet.currency(), wallet.decimals());
    }
}
