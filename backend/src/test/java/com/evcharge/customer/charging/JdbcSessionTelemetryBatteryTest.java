package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcSessionTelemetryBatteryTest extends PostgresTestBase {

    @Autowired
    SessionTelemetry telemetry;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void theFinalLevelsOfThePlatformReplaceTheLastPolledOneAndNonsenseIsIgnored() {
        AdminTestData data = new AdminTestData(jdbc);
        UUID customer = data.customer("GA", "ACTIVE");
        data.session("GA", customer, 91, "COMPLETED", "4.515", 900L);
        UUID id = TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select id from charging_session where customer_id = ?", UUID.class, customer));

        TenantContext.runAs("GA", () -> {
            telemetry.recordBatteryLevels(id, new BigDecimal("91"), new BigDecimal("99"));
            telemetry.recordBatteryLevels(id, null, new BigDecimal("100"));
            telemetry.recordBatteryLevels(id, new BigDecimal("0"), new BigDecimal("150"));
        });

        var last = TenantContext.callAs("GA", () -> telemetry.lastReadingOf(id));
        assertThat(last.startBatteryPercent()).isEqualByComparingTo("91");
        assertThat(last.batteryPercent()).isEqualByComparingTo("100");
    }

    @Test
    void aReadingWithALevelOfZeroStillRecordsTheEnergyAndTheProgress() {
        AdminTestData data = new AdminTestData(jdbc);
        UUID customer = data.customer("GA", "ACTIVE");
        data.session("GA", customer, 91, "ACTIVE", "0", 900L);
        UUID id = TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select id from charging_session where customer_id = ?", UUID.class, customer));

        TenantContext.runAs("GA", () -> telemetry.recordReading(id, new Metering(new BigDecimal("0.464"),
                new BigDecimal("7"), new BigDecimal("0"), 120L, new BigDecimal("230"), null, Instant.now())));

        var progress = TenantContext.callAs("GA", () -> telemetry.progressOf(id));
        assertThat(progress.energyKwh()).isEqualByComparingTo("0.464");
        assertThat(progress.at()).isNotNull();
    }

    @Test
    void theReadingClockMovesOnlyWhenANewSampleArrives() throws Exception {
        AdminTestData data = new AdminTestData(jdbc);
        UUID customer = data.customer("GA", "ACTIVE");
        data.session("GA", customer, 91, "ACTIVE", "0", 900L);
        UUID id = TenantContext.callAs("GA", () -> jdbc.queryForObject(
                "select id from charging_session where customer_id = ?", UUID.class, customer));
        Instant sampled = Instant.parse("2026-11-09T10:00:00Z");
        Metering sample = new Metering(new BigDecimal("1"), null, null, null, null, null, sampled);

        assertThat(TenantContext.callAs("GA", () -> telemetry.readingAtOf(id))).isEmpty();
        TenantContext.runAs("GA", () -> telemetry.recordReading(id, sample));
        Instant first = TenantContext.callAs("GA", () -> telemetry.readingAtOf(id)).orElseThrow();
        Thread.sleep(50);
        TenantContext.runAs("GA", () -> telemetry.recordReading(id, sample));
        assertThat(TenantContext.callAs("GA", () -> telemetry.readingAtOf(id)).orElseThrow()).isEqualTo(first);

        Metering later = new Metering(new BigDecimal("1.1"), null, null, null, null, null, sampled.plusSeconds(10));
        TenantContext.runAs("GA", () -> telemetry.recordReading(id, later));
        assertThat(TenantContext.callAs("GA", () -> telemetry.readingAtOf(id)).orElseThrow()).isAfter(first);
    }
}
