package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.application.port.SessionTelemetry;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
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
}
