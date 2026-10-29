package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.charging.CommissionPage;
import com.evcharge.customer.charging.CommissionQuery;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.charging.application.FeeCalculator;
import com.evcharge.customer.charging.application.FeeSettings;
import com.evcharge.customer.shared.ReportingWindow;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcCommissionLogTest extends PostgresTestBase {

    @Autowired
    JdbcClient client;

    @Autowired
    JdbcTemplate jdbc;

    private JdbcCommissionLog log(Instant start) {
        FeeSettings fees = new FeeSettings(BigDecimal.ZERO, new BigDecimal("2"));
        return new JdbcCommissionLog(client, new FeeCalculator(fees), fees, new ReportingWindow(start));
    }

    @Test
    void theServiceFeeOfEachSessionAddsUpToTheTotalAndIgnoresSessionsBeforeTheReportingStart() {
        AdminTestData data = new AdminTestData(jdbc);
        UUID customer = data.customer("GA", "ACTIVE", "Zq", "Fee", null, Instant.now());
        Instant start = Instant.now().minus(5, ChronoUnit.MINUTES);
        data.session("GA", customer, 11, "COMPLETED", "0.385", 135L);
        data.session("GA", customer, 12, "COMPLETED", "1.9", 600L);

        CommissionPage everything = TenantContext.callAs("GA", () -> log(Instant.EPOCH).search(new CommissionQuery(null, LogFilter.NONE, 10)));
        CommissionPage recent = TenantContext.callAs("GA", () -> log(start).search(new CommissionQuery(null, new LogFilter(null, null, customer), 10)));
        CommissionPage future = TenantContext.callAs("GA", () -> log(Instant.now().plus(1, ChronoUnit.DAYS)).search(new CommissionQuery(null, LogFilter.NONE, 10)));

        var mine = recent.items().stream().filter(item -> item.customerName().equals("Zq Fee")).toList();

        assertThat(mine).extracting(item -> item.serviceFeeMinor()).containsExactlyInAnyOrder(3L, 12L);
        assertThat(mine).extracting(item -> item.totalMinor()).containsExactlyInAnyOrder(138L, 612L);
        assertThat(recent.totals().serviceFeeMinor()).isGreaterThanOrEqualTo(15);
        assertThat(everything.totals().sessions()).isGreaterThanOrEqualTo(recent.totals().sessions());
        assertThat(future.totals().sessions()).isZero();
    }
}
