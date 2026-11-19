package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.CustomerBreakdown;
import com.evcharge.customer.account.CustomerCounts;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcCustomerBreakdown implements CustomerBreakdown {

    private static final String STAFF = "exists (select 1 from customer_complimentary f where f.customer_id = c.id "
            + "and f.category = 'NDZELA_STAFF')";
    private static final String DRIVER = "exists (select 1 from partner_link p where p.customer_id = c.id "
            + "and p.partner = 'NDZELA')";

    private final JdbcClient jdbc;

    JdbcCustomerBreakdown(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CustomerCounts counts() {
        return jdbc.sql("select count(*) as total, "
                        + "count(*) filter (where not " + STAFF + " and not " + DRIVER + ") as members, "
                        + "count(*) filter (where not " + STAFF + " and " + DRIVER + ") as drivers, "
                        + "count(*) filter (where " + STAFF + ") as staff, "
                        + "count(*) filter (where c.status = 'BLOCKED') as suspended "
                        + "from customer c where c.status <> 'DELETED' "
                        + "and c.country_code = current_setting('app.country', true)")
                .query((rs, rowNum) -> new CustomerCounts(rs.getLong("total"), rs.getLong("members"),
                        rs.getLong("drivers"), rs.getLong("staff"), rs.getLong("suspended"))).single();
    }
}
