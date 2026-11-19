package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.ActivityFlows;
import com.evcharge.customer.charging.DailyActivity;
import com.evcharge.customer.shared.Bucket;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcActivityFlowsTest extends PostgresTestBase {

    @Autowired
    ActivityFlows flows;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aSessionStartedJustBeforeMidnightUtcCountsOnTheNextLocalDay() {
        UUID customer = new AdminTestData(jdbc).customer("GA", "ACTIVE");
        Timestamp lateEvening = Timestamp.from(Instant.parse("2031-03-10T23:55:00Z"));
        TenantContext.runAs("GA", () -> jdbc.update("insert into charging_session (id, customer_id, station_id, "
                + "connector_id, id_tag, state, started_at, created_at, updated_at) values (?, ?, ?, 1, 'Z-x', "
                + "'ACTIVE', ?, ?, ?)", UUID.randomUUID(), customer, "CP-" + UUID.randomUUID(), lateEvening,
                lateEvening, lateEvening));

        List<DailyActivity> days = TenantContext.callAs("GA", () -> flows.between(
                Instant.parse("2031-03-09T00:00:00Z"), Instant.parse("2031-03-13T00:00:00Z"), Bucket.DAY));

        assertThat(days).extracting(DailyActivity::day).contains(LocalDate.parse("2031-03-11"))
                .doesNotContain(LocalDate.parse("2031-03-10"));
    }
}
