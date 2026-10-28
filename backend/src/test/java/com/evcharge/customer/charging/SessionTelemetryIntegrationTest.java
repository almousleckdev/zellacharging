package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.SessionProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SessionTelemetryIntegrationTest extends PostgresTestBase {

    @Autowired
    SessionTelemetry telemetry;

    @Autowired
    JdbcTemplate jdbc;

    private UUID session;

    @BeforeEach
    void liveSession() {
        AdminTestData data = new AdminTestData(jdbc);
        UUID customer = data.customer("GA", "ACTIVE");
        session = UUID.randomUUID();
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "started_at, created_at, updated_at) values (?, ?, ?, 1, 'Z-x', 'ACTIVE', now(), now(), now())",
                session, customer, "CP-" + session);
    }

    private Metering reading(String kwh, String battery) {
        return new Metering(kwh == null ? null : new BigDecimal(kwh), new BigDecimal("7"),
                battery == null ? null : new BigDecimal(battery), 60L, null, null, Instant.now());
    }

    @Test
    void aReadingWithoutEnergyStillRecordsTheBattery() {
        telemetry.recordReading(session, reading(null, "42"));

        assertThat(telemetry.lastReadingOf(session).batteryPercent()).isEqualByComparingTo("42");
        assertThat(telemetry.progressOf(session)).isEqualTo(SessionProgress.none());
    }

    @Test
    void theStartingBatteryIsKeptWhileTheLatestOneFollows() {
        telemetry.recordReading(session, reading("0.5", "20"));
        telemetry.recordReading(session, reading("3.0", "65"));

        assertThat(telemetry.lastReadingOf(session).startBatteryPercent()).isEqualByComparingTo("20");
        assertThat(telemetry.lastReadingOf(session).batteryPercent()).isEqualByComparingTo("65");
    }

    @Test
    void progressOnlyMovesWhenTheEnergyGrows() {
        telemetry.recordReading(session, reading("1.5", "40"));
        Instant first = telemetry.progressOf(session).at();
        telemetry.recordReading(session, reading("1.5", "40"));

        assertThat(telemetry.progressOf(session).at()).isEqualTo(first);
        assertThat(telemetry.progressOf(session).energyKwh()).isEqualByComparingTo("1.5");

        telemetry.recordReading(session, reading("2.0", "41"));
        assertThat(telemetry.progressOf(session).energyKwh()).isEqualByComparingTo("2.0");
    }
}
