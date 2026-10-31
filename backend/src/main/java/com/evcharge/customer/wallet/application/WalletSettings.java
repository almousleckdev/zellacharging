package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import org.springframework.stereotype.Component;

@Component
public class WalletSettings {

    private final WalletDefaults defaults;
    private final RuntimeSettings runtime;

    WalletSettings(WalletDefaults defaults, RuntimeSettings runtime) {
        this.defaults = defaults;
        this.runtime = runtime;
    }

    public String currency() {
        return defaults.currency();
    }

    public int decimals() {
        return defaults.decimals();
    }

    public long minimumStartMinor() {
        return runtime.number(SettingKey.MINIMUM_START, defaults.minimumStartMinor());
    }

    public long maximumStartMinor() {
        return runtime.number(SettingKey.MAXIMUM_START, defaults.maximumStartMinor());
    }

    public long minTopupMinor() {
        return runtime.number(SettingKey.MIN_TOPUP, defaults.minTopupMinor());
    }

    public long maxTopupMinor() {
        return runtime.number(SettingKey.MAX_TOPUP, defaults.maxTopupMinor());
    }
}
