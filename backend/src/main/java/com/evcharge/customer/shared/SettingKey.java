package com.evcharge.customer.shared;

import java.util.Arrays;
import java.util.Optional;

public enum SettingKey {
    MINIMUM_START("wallet", "minimumStartMinor", Kind.MONEY),
    MAXIMUM_START("wallet", "maximumStartMinor", Kind.MONEY),
    MIN_TOPUP("wallet", "minTopupMinor", Kind.MONEY),
    MAX_TOPUP("wallet", "maxTopupMinor", Kind.MONEY),
    WITHDRAWALS_ENABLED("wallet", "withdrawalsEnabled", Kind.FLAG),
    WITHDRAWAL_MINIMUM("wallet", "withdrawalMinimumMinor", Kind.MONEY),
    WITHDRAWAL_MAXIMUM("wallet", "withdrawalMaximumMinor", Kind.MONEY),
    WITHDRAWAL_DAILY_MAXIMUM("wallet", "withdrawalDailyMaximumMinor", Kind.MONEY),
    PRICE_PER_KWH("charging", "pricePerKwhMinor", Kind.MONEY),
    SERVICE_FEE("charging", "serviceFeePercent", Kind.PERCENT),
    VAT("charging", "vatPercent", Kind.PERCENT);

    public enum Kind {
        MONEY,
        PERCENT,
        FLAG
    }

    private final String section;
    private final String field;
    private final Kind kind;

    SettingKey(String section, String field, Kind kind) {
        this.section = section;
        this.field = field;
        this.kind = kind;
    }

    public String section() {
        return section;
    }

    public String field() {
        return field;
    }

    public Kind kind() {
        return kind;
    }

    public String storageKey() {
        return section + "." + field;
    }

    public static Optional<SettingKey> of(String section, String field) {
        return Arrays.stream(values()).filter(key -> key.section.equals(section) && key.field.equals(field)).findFirst();
    }
}
