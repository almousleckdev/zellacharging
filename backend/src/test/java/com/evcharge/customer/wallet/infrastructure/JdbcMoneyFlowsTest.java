package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.shared.Bucket;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.DailyMoney;
import com.evcharge.customer.wallet.MoneyFlows;
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
class JdbcMoneyFlowsTest extends PostgresTestBase {

    @Autowired
    MoneyFlows flows;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aCaptureBelongsToTheLocalDayTheChargeStartedNotTheDayItWasBilled() {
        UUID customer = new AdminTestData(jdbc).customer("GA", "ACTIVE");
        UUID session = UUID.randomUUID();
        Timestamp started = Timestamp.from(Instant.parse("2032-03-10T21:38:00Z"));
        Timestamp billed = Timestamp.from(Instant.parse("2032-03-10T23:20:00Z"));
        TenantContext.runAs("GA", () -> {
            jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                    + "started_at, created_at, updated_at) values (?, ?, ?, 1, 'Z-x', 'COMPLETED', ?, ?, ?)",
                    session, customer, "CP-" + UUID.randomUUID(), started, started, billed);
            jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, session_id, "
                    + "idempotency_key, created_at) values (?, ?, 'CAPTURE', 7393, ?, ?, ?)", UUID.randomUUID(),
                    customer, session, UUID.randomUUID().toString(), billed);
        });

        List<DailyMoney> days = TenantContext.callAs("GA", () -> flows.between(
                Instant.parse("2032-03-09T00:00:00Z"), Instant.parse("2032-03-13T00:00:00Z"), Bucket.DAY));

        assertThat(days).filteredOn(day -> day.day().equals(LocalDate.parse("2032-03-10")))
                .extracting(DailyMoney::capturedMinor).containsExactly(7393L);
        assertThat(days).extracting(DailyMoney::day).doesNotContain(LocalDate.parse("2032-03-11"));
    }
}
