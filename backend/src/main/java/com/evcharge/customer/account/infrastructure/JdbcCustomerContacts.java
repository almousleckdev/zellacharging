package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.CustomerContacts;
import com.evcharge.customer.account.CustomerMatch;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcCustomerContacts implements CustomerContacts {

    private static final String NOTHING = "";

    private final JdbcClient jdbc;

    JdbcCustomerContacts(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CustomerMatch> activeByEmailOrPhone(Collection<String> emails, Collection<String> phonesE164) {
        if (emails.isEmpty() && phonesE164.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("select id, first_name, last_name, email, phone_e164 from customer "
                        + "where status = 'ACTIVE' and country_code = current_setting('app.country') "
                        + "and (email in (:emails) or phone_e164 in (:phones))")
                .param("emails", orNothing(emails)).param("phones", orNothing(phonesE164))
                .query((rs, row) -> new CustomerMatch(rs.getObject("id", UUID.class), rs.getString("first_name"),
                        rs.getString("last_name"), rs.getString("email"), rs.getString("phone_e164")))
                .list();
    }

    @Override
    public boolean activeExists(UUID customerId) {
        return jdbc.sql("select count(*) from customer where id = :id and status = 'ACTIVE' "
                        + "and country_code = current_setting('app.country')")
                .param("id", customerId).query(Integer.class).single() > 0;
    }

    private static Collection<String> orNothing(Collection<String> values) {
        return values.isEmpty() ? List.of(NOTHING) : values;
    }
}
