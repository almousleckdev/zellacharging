package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class WithdrawalSettings {

    private final WithdrawalDefaults defaults;
    private final RuntimeSettings runtime;

    WithdrawalSettings(WithdrawalDefaults defaults, RuntimeSettings runtime) {
        this.defaults = defaults;
        this.runtime = runtime;
    }

    public boolean enabled() {
        return runtime.flag(SettingKey.WITHDRAWALS_ENABLED, defaults.enabled());
    }

    public long minimumMinor() {
        return runtime.number(SettingKey.WITHDRAWAL_MINIMUM, defaults.minimumMinor());
    }

    public long maximumMinor() {
        return runtime.number(SettingKey.WITHDRAWAL_MAXIMUM, defaults.maximumMinor());
    }

    public long dailyMaximumMinor() {
        return runtime.number(SettingKey.WITHDRAWAL_DAILY_MAXIMUM, defaults.dailyMaximumMinor());
    }

    public Duration recheckAfter() {
        return defaults.recheckAfter();
    }

    public Duration reviewAfter() {
        return defaults.reviewAfter();
    }
}
