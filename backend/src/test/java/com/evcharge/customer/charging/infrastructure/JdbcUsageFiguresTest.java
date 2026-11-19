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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcUsageFiguresTest extends PostgresTestBase {

    @Autowired
    UsageFigures usage;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void aCustomerGetsTheirPaidEnergyApartFromTheStaffEnergyWhichIsNeverCollected() {
        AdminTestData data = new AdminTestData(jdbc);
        UUID driver = data.customer("GA", "ACTIVE");
        UUID staff = data.customer("GA", "ACTIVE");
        data.session("GA", driver, 1, "COMPLETED", "10.5", 3675L);
        data.session("GA", driver, 2, "ACTIVE", null, null);
        data.complimentarySession("GA", staff, "6.0", 2100L);
        TenantContext.runAs("GA", () -> jdbc.update("insert into customer_complimentary (customer_id, granted_by, "
                + "reason, granted_at) values (?, ?, 'staff', now())", staff, UUID.randomUUID()));
        jdbc.update("update customer_complimentary set category = 'NDZELA_STAFF' where customer_id = ?", staff);

        UsageSummary mine = TenantContext.callAs("GA", () -> usage.summary(null, driver));
        UsageSummary theirs = TenantContext.callAs("GA", () -> usage.summary(null, staff));

        assertThat(mine.today().sessions()).isEqualTo(2);
        assertThat(mine.today().energyKwh()).isEqualByComparingTo("10.5");
        assertThat(mine.today().collectedMinor()).isEqualTo(3675L);
        assertThat(mine.week().collectedMinor()).isEqualTo(3675L);
        assertThat(mine.month().sessions()).isEqualTo(2);
        assertThat(mine.yesterday().sessions()).isZero();
        assertThat(mine.today().staffEnergyKwh()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(theirs.today().energyKwh()).isEqualByComparingTo("6.0");
        assertThat(theirs.today().staffEnergyKwh()).isEqualByComparingTo("6.0");
        assertThat(theirs.today().collectedMinor()).isZero();
    }

    @Test
    void aCustomerWithoutSessionsStillGetsZeroFigures() {
        UUID nobody = new AdminTestData(jdbc).customer("GA", "ACTIVE");

        UsageSummary empty = TenantContext.callAs("GA", () -> usage.summary(null, nobody));

        assertThat(empty.today().sessions()).isZero();
        assertThat(empty.month().energyKwh()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
