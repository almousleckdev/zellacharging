package com.evcharge.customer.shared;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface RuntimeSettings {

    Optional<String> find(SettingKey key);

    void put(SettingKey key, String value, UUID updatedBy);

    void clear(SettingKey key);

    default long number(SettingKey key, long fallback) {
        return find(key).map(Long::parseLong).orElse(fallback);
    }

    default Long optionalNumber(SettingKey key, Long fallback) {
        return find(key).map(Long::valueOf).orElse(fallback);
    }

    default BigDecimal decimal(SettingKey key, BigDecimal fallback) {
        return find(key).map(BigDecimal::new).orElse(fallback);
    }

    default boolean flag(SettingKey key, boolean fallback) {
        return find(key).map(Boolean::parseBoolean).orElse(fallback);
    }
}
