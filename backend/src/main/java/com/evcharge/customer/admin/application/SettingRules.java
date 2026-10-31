package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.SettingKey;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

final class SettingRules {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final List<SettingKey[]> ORDERED = List.of(
            new SettingKey[]{SettingKey.MINIMUM_START, SettingKey.MAXIMUM_START},
            new SettingKey[]{SettingKey.MIN_TOPUP, SettingKey.MAX_TOPUP},
            new SettingKey[]{SettingKey.WITHDRAWAL_MINIMUM, SettingKey.WITHDRAWAL_MAXIMUM},
            new SettingKey[]{SettingKey.WITHDRAWAL_MAXIMUM, SettingKey.WITHDRAWAL_DAILY_MAXIMUM});

    private SettingRules() {
    }

    static Object parse(SettingKey key, Object raw) {
        return switch (key.kind()) {
            case FLAG -> flag(key, raw);
            case PERCENT -> percent(key, raw);
            case MONEY -> money(key, raw);
        };
    }

    static void requireOrdered(Map<String, Object> merged) {
        for (SettingKey[] pair : ORDERED) {
            Object low = merged.get(pair[0].field());
            Object high = merged.get(pair[1].field());
            if (low instanceof Long lowest && high instanceof Long highest && lowest > highest) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "SETTING_RANGE_INVALID",
                        pair[0].field() + " must not exceed " + pair[1].field());
            }
        }
    }

    private static Boolean flag(SettingKey key, Object raw) {
        if (raw instanceof Boolean value) {
            return value;
        }
        throw invalid(key);
    }

    private static BigDecimal percent(SettingKey key, Object raw) {
        if (raw instanceof Number number) {
            BigDecimal value = new BigDecimal(number.toString());
            if (value.signum() >= 0 && value.compareTo(HUNDRED) <= 0 && value.scale() <= 2) {
                return value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros();
            }
        }
        throw invalid(key);
    }

    private static Long money(SettingKey key, Object raw) {
        if (raw instanceof Number number && number.doubleValue() == number.longValue()) {
            long value = number.longValue();
            long floor = key == SettingKey.PRICE_PER_KWH ? 1 : 0;
            if (value >= floor) {
                return value;
            }
        }
        throw invalid(key);
    }

    private static ApiException invalid(SettingKey key) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SETTING", "Invalid value for " + key.field());
    }
}
