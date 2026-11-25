package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.UsageFigures;
import com.evcharge.customer.charging.UsageSummary;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcUsageCountingRuleTest extends PostgresTestBase {

    private static final String LOCAL_MIDNIGHT = "(date_trunc('day', now() at time zone "
            + "(select time_zone from country where code = 'GA')) at time zone "
            + "(select time_zone from country where code = 'GA'))";

    @Autowired
    UsageFigures usage;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aStartThatNeverReachedTheChargerIsNotACharge() {
        UUID driver = new AdminTestData(jdbc).customer("GA", "ACTIVE");
        insert(driver, 1, "COMPLETED", 910001, "now()", "now()");
        insert(driver, 2, "FAILED", null, "null", "now()");

        UsageSummary mine = TenantContext.callAs("GA", () -> usage.summary(null, driver));

        assertThat(mine.today().sessions()).isEqualTo(1);
        assertThat(mine.month().sessions()).isEqualTo(1);
    }

    @Test
    void aChargeBelongsToTheDayItStartedNotTheDayItWasRequested() {
        UUID driver = new AdminTestData(jdbc).customer("GA", "ACTIVE");
        insert(driver, 3, "COMPLETED", 910002, LOCAL_MIDNIGHT + " + interval '1 second'",
                LOCAL_MIDNIGHT + " - interval '1 minute'");

        UsageSummary mine = TenantContext.callAs("GA", () -> usage.summary(null, driver));

        assertThat(mine.today().sessions()).isEqualTo(1);
        assertThat(mine.yesterday().sessions()).isZero();
    }

    private void insert(UUID customer, int connector, String state, Integer transactionId, String startedAt,
                        String createdAt) {
        TenantContext.runAs("GA", () -> jdbc.update("insert into charging_session (id, customer_id, station_id, "
                + "connector_id, id_tag, state, transaction_id, started_at, energy_kwh, created_at, updated_at) "
                + "values (?, ?, ?, ?, 'Z-x', ?, ?, " + startedAt + ", 0, " + createdAt + ", now())",
                UUID.randomUUID(), customer, "CP-" + UUID.randomUUID(), connector, state, transactionId));
    }
}
