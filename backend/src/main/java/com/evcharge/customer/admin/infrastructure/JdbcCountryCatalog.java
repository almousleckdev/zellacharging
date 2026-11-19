package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.shared.Country;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class JdbcCountryCatalog implements CountryCatalog {

    private final JdbcClient jdbc;

    JdbcCountryCatalog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Country> all() {
        return jdbc.sql("select code, name, currency, currency_decimals, phone_region, time_zone from country order by code")
                .query((rs, rowNum) -> new Country(rs.getString("code").trim(), rs.getString("name"),
                        rs.getString("currency").trim(), rs.getInt("currency_decimals"),
                        rs.getString("phone_region").trim(), rs.getString("time_zone"))).list();
    }
}
