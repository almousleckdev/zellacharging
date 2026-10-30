package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.WalletBalance;

import java.util.List;
import java.util.Optional;

record WalletResponse(long balanceMinor, long reservedMinor, long availableMinor, String currency, int decimals,
                      boolean withdrawalsEnabled, String payoutNumber, String payoutOperator,
                      List<PayoutNumber> payoutNumbers) {

    record PayoutNumber(String msisdn, String operator) {
    }

    static WalletResponse from(WalletBalance wallet, boolean withdrawalsEnabled, Optional<Topup> payoutSource,
                               List<Topup> payoutSources) {
        return new WalletResponse(wallet.balanceMinor(), wallet.reservedMinor(), wallet.availableMinor(),
                wallet.currency(), wallet.decimals(), withdrawalsEnabled,
                payoutSource.map(Topup::msisdn).orElse(null),
                payoutSource.map(topup -> topup.operator().name()).orElse(null),
                payoutSources.stream().map(t -> new PayoutNumber(t.msisdn(), t.operator().name())).toList());
    }
}
