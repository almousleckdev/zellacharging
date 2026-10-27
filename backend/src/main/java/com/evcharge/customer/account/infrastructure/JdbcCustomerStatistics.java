package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.CustomerFigures;
import com.evcharge.customer.account.CustomerStatistics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcCustomerStatistics implements CustomerStatistics {

    private final JdbcClient jdbc;

    JdbcCustomerStatistics(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public CustomerFigures figures() {
        return jdbc.sql("select count(*) filter (where status <> 'DELETED') as total, "
                        + "count(*) filter (where status = 'ACTIVE') as active, "
                        + "count(*) filter (where status <> 'DELETED' and created_at >= now() - interval '30 days') "
                        + "as recent from customer "
                        + "where country_code = current_setting('app.country', true)")
                .query((rs, rowNum) -> new CustomerFigures(rs.getLong("total"), rs.getLong("active"),
                        rs.getLong("recent"))).single();
    }
}
