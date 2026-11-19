package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.CustomerBreakdown;
import com.evcharge.customer.account.CustomerCounts;
import com.evcharge.customer.admin.AdminTestData;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JdbcCustomerBreakdownTest extends PostgresTestBase {

    @Autowired
    CustomerBreakdown breakdown;

    @Autowired
    JdbcTemplate jdbc;

    private CustomerCounts counts() {
        return TenantContext.callAs("GA", breakdown::counts);
    }

    @Test
    void everyCustomerIsExactlyOneOfPublicNdzelaDriverOrNdzelaStaffAndDeletedOnesAreIgnored() {
        AdminTestData data = new AdminTestData(jdbc);
        CustomerCounts before = counts();
        UUID driver = data.customer("GA", "ACTIVE");
        UUID staff = data.customer("GA", "ACTIVE");
        UUID staffAndDriver = data.customer("GA", "ACTIVE");
        data.customer("GA", "ACTIVE");
        data.customer("GA", "BLOCKED", "Zq", "Blocked", null, Instant.now());
        data.customer("GA", "DELETED");
        TenantContext.runAs("GA", () -> {
            for (UUID id : new UUID[] {driver, staffAndDriver}) {
                jdbc.update("insert into partner_link (partner, customer_id, linked_at) values ('NDZELA', ?, now())", id);
            }
            for (UUID id : new UUID[] {staff, staffAndDriver}) {
                jdbc.update("insert into customer_complimentary (customer_id, granted_by, reason, category, granted_at) "
                        + "values (?, ?, 'staff', 'NDZELA_STAFF', now())", id, UUID.randomUUID());
            }
        });

        CustomerCounts after = counts();

        assertThat(after.total() - before.total()).isEqualTo(5);
        assertThat(after.ndzelaDrivers() - before.ndzelaDrivers()).isEqualTo(1);
        assertThat(after.ndzelaStaff() - before.ndzelaStaff()).isEqualTo(2);
        assertThat(after.publicUsers() - before.publicUsers()).isEqualTo(2);
        assertThat(after.suspended() - before.suspended()).isEqualTo(1);
        assertThat(after.publicUsers() + after.ndzelaDrivers() + after.ndzelaStaff()).isEqualTo(after.total());
    }
}
