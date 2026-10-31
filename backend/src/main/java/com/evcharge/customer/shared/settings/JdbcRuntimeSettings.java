package com.evcharge.customer.shared.settings;

import com.evcharge.customer.shared.RuntimeSettings;
import com.evcharge.customer.shared.SettingKey;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
class JdbcRuntimeSettings implements RuntimeSettings {

    private static final Duration FRESHNESS = Duration.ofSeconds(15);

    private final JdbcClient jdbc;
    private final Clock clock;
    private volatile Map<String, String> snapshot = Map.of();
    private volatile Instant loadedAt = Instant.MIN;

    JdbcRuntimeSettings(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public Optional<String> find(SettingKey key) {
        if (loadedAt.plus(FRESHNESS).isBefore(clock.instant())) {
            reload();
        }
        return Optional.ofNullable(snapshot.get(key.storageKey()));
    }

    @Override
    public void put(SettingKey key, String value, UUID updatedBy) {
        jdbc.sql("""
                        insert into platform_setting (key, value, updated_by, updated_at)
                        values (:key, :value, :by, :at)
                        on conflict (key) do update set value = excluded.value, updated_by = excluded.updated_by,
                            updated_at = excluded.updated_at""")
                .param("key", key.storageKey()).param("value", value).param("by", updatedBy)
                .param("at", Timestamps.toDb(clock.instant())).update();
        reload();
    }

    @Override
    public void clear(SettingKey key) {
        jdbc.sql("delete from platform_setting where key = :key").param("key", key.storageKey()).update();
        reload();
    }

    private synchronized void reload() {
        Map<String, String> loaded = new HashMap<>();
        jdbc.sql("select key, value from platform_setting").query((rs, rowNum) -> {
            loaded.put(rs.getString("key"), rs.getString("value"));
            return null;
        }).list();
        snapshot = Map.copyOf(loaded);
        loadedAt = clock.instant();
    }
}
