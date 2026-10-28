package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionProgress;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcSessionTelemetry implements SessionTelemetry {

    private final JdbcClient jdbc;

    JdbcSessionTelemetry(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void recordReading(UUID id, Metering reading) {
        jdbc.sql("""
                update charging_session set battery_percent = coalesce(:battery, battery_percent),
                    start_battery_percent = coalesce(start_battery_percent, cast(:battery as numeric)),
                    last_voltage = coalesce(:voltage, last_voltage),
                    last_power_kw = coalesce(:power, last_power_kw),
                    progress_at = case when cast(:kwh as numeric) is not null and cast(:kwh as numeric) > coalesce(last_energy_kwh, -1)
                        then now() else progress_at end,
                    last_energy_kwh = case when cast(:kwh as numeric) is not null and cast(:kwh as numeric) > coalesce(last_energy_kwh, -1)
                        then cast(:kwh as numeric) else last_energy_kwh end
                where id = :id
                """)
                .param("battery", reading.stateOfChargePercent()).param("voltage", reading.voltage())
                .param("power", reading.powerKw()).param("kwh", reading.totalKwh()).param("id", id).update();
    }

    @Override
    public LastReading lastReadingOf(UUID id) {
        return jdbc.sql("select start_battery_percent, battery_percent, last_voltage, last_power_kw from charging_session "
                + "where id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new LastReading(rs.getBigDecimal("start_battery_percent"),
                        rs.getBigDecimal("battery_percent"),
                        rs.getBigDecimal("last_voltage"), rs.getBigDecimal("last_power_kw")))
                .optional().orElse(LastReading.empty());
    }

    @Override
    public SessionProgress progressOf(UUID id) {
        return jdbc.sql("select last_energy_kwh, progress_at from charging_session where id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new SessionProgress(rs.getBigDecimal("last_energy_kwh"),
                        rs.getTimestamp("progress_at") == null ? null : rs.getTimestamp("progress_at").toInstant()))
                .optional().orElse(SessionProgress.none());
    }

    @Override
    public void recordPrice(UUID id, long pricePerKwhMinor) {
        jdbc.sql("update charging_session set price_per_kwh_minor = :price where id = :id")
                .param("price", pricePerKwhMinor).param("id", id).update();
    }

    @Override
    public Optional<Long> priceOf(UUID id) {
        return jdbc.sql("select price_per_kwh_minor from charging_session where id = :id")
                .param("id", id).query(Long.class).optional();
    }
}
