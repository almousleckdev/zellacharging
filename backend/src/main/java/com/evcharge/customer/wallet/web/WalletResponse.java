package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.WalletBalance;

import java.util.Optional;

record WalletResponse(long balanceMinor, long reservedMinor, long availableMinor, String currency, int decimals,
                      boolean withdrawalsEnabled, String payoutNumber, String payoutOperator) {

    static WalletResponse from(WalletBalance wallet, boolean withdrawalsEnabled, Optional<Topup> payoutSource) {
        return new WalletResponse(wallet.balanceMinor(), wallet.reservedMinor(), wallet.availableMinor(),
                wallet.currency(), wallet.decimals(), withdrawalsEnabled,
                payoutSource.map(Topup::msisdn).orElse(null),
                payoutSource.map(topup -> topup.operator().name()).orElse(null));
    }
}
