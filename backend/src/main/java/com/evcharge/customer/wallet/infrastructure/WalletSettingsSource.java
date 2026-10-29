package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.SettingsSource;
import com.evcharge.customer.wallet.application.PaymentSettings;
import com.evcharge.customer.wallet.application.WalletSettings;
import com.evcharge.customer.wallet.application.WithdrawalSettings;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
class WalletSettingsSource implements SettingsSource {

    private final WalletSettings wallet;
    private final WithdrawalSettings withdrawals;
    private final PaymentSettings payments;

    WalletSettingsSource(WalletSettings wallet, WithdrawalSettings withdrawals, PaymentSettings payments) {
        this.wallet = wallet;
        this.withdrawals = withdrawals;
        this.payments = payments;
    }

    @Override
    public String section() {
        return "wallet";
    }

    @Override
    public Map<String, Object> values() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("minimumStartMinor", wallet.minimumStartMinor());
        values.put("maximumStartMinor", wallet.maximumStartMinor());
        values.put("minTopupMinor", wallet.minTopupMinor());
        values.put("maxTopupMinor", wallet.maxTopupMinor());
        values.put("withdrawalsEnabled", withdrawals.enabled());
        values.put("withdrawalMinimumMinor", withdrawals.minimumMinor());
        values.put("withdrawalMaximumMinor", withdrawals.maximumMinor());
        values.put("withdrawalDailyMaximumMinor", withdrawals.dailyMaximumMinor());
        values.put("paymentProvider", payments.provider());
        values.put("sandboxAllowed", payments.allowSandbox());
        return values;
    }
}
