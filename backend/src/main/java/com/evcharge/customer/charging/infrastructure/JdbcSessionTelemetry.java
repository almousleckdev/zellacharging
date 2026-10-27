package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.LastReading;
import com.evcharge.customer.charging.domain.Metering;
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
                    last_voltage = coalesce(:voltage, last_voltage),
                    last_power_kw = coalesce(:power, last_power_kw)
                where id = :id
                """)
                .param("battery", reading.stateOfChargePercent()).param("voltage", reading.voltage())
                .param("power", reading.powerKw()).param("id", id).update();
    }

    @Override
    public LastReading lastReadingOf(UUID id) {
        return jdbc.sql("select battery_percent, last_voltage, last_power_kw from charging_session where id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new LastReading(rs.getBigDecimal("battery_percent"),
                        rs.getBigDecimal("last_voltage"), rs.getBigDecimal("last_power_kw")))
                .optional().orElse(LastReading.empty());
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
