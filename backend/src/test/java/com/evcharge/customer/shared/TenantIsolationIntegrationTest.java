package com.evcharge.customer.shared;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TenantIsolationIntegrationTest extends PostgresTestBase {

    private static final String OTHER = "ML";

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    MockMvc mvc;

    private UUID gabonese;
    private UUID session;

    @BeforeEach
    void seed() {
        jdbc.update("update country set active = true where code = ?", OTHER);
        gabonese = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", gabonese, gabonese + "@example.com");
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XAF', 0, 5000, now())", gabonese);
        session = UUID.randomUUID();
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "created_at, updated_at) values (?, ?, 'CP-1', 1, ?, 'COMPLETED', now(), now())",
                session, gabonese, "Z-" + gabonese.toString().substring(0, 8));
    }

    private <T> T asOther(java.util.function.Supplier<T> query) {
        return TenantContext.callAs(OTHER, query);
    }

    private int countAsOther(String table, String ownerColumn) {
        return asOther(() -> jdbc.queryForObject("select count(*) from " + table + " where " + ownerColumn + " = ?",
                Integer.class, gabonese));
    }

    @Test
    void anotherCountryCannotSeeCustomerWalletOrSessionRows() {
        assertThat(countAsOther("customer", "id")).isZero();
        assertThat(countAsOther("wallet", "customer_id")).isZero();
        assertThat(countAsOther("charging_session", "customer_id")).isZero();
        assertThat(jdbc.queryForObject("select count(*) from wallet where customer_id = ?", Integer.class,
                gabonese)).isOne();
    }

    @Test
    void withoutATenantNothingIsVisibleAndNothingCanBeWritten() {
        TenantContext.clear();
        assertThat(jdbc.queryForObject("select count(*) from customer", Integer.class)).isZero();
        assertThatThrownBy(() -> jdbc.update("insert into wallet (customer_id, currency, decimals, created_at) "
                + "values (?, 'XAF', 0, now())", gabonese)).isInstanceOf(DataAccessException.class);
    }

    @Test
    void aRowCannotBeWrittenIntoAnotherCountry() {
        assertThatThrownBy(() -> asOther(() -> jdbc.update("insert into customer (id, country_code, first_name, "
                + "last_name, email, password_hash, status, created_at) values (?, 'GA', 'A', 'B', 'x@y.z', 'x', "
                + "'ACTIVE', now())", UUID.randomUUID()))).isInstanceOf(DataAccessException.class);
    }

    @Test
    void aChildRowCannotPointAtACustomerOfAnotherCountry() {
        assertThatThrownBy(() -> asOther(() -> jdbc.update("insert into wallet (customer_id, currency, decimals, "
                + "created_at) values (?, 'XOF', 0, now())", gabonese))).isInstanceOf(DataAccessException.class);
    }

    @Test
    void anotherCountryCannotUpdateOrDeleteTheseRows() {
        int updated = asOther(() -> jdbc.update("update wallet set balance_minor = 0 where customer_id = ?", gabonese));
        int deleted = asOther(() -> jdbc.update("delete from customer where id = ?", gabonese));
        assertThat(updated).isZero();
        assertThat(deleted).isZero();
        assertThat(jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class,
                gabonese)).isEqualTo(5000);
    }

    @Test
    void theSameEmailMayExistInTwoCountries() {
        String email = "shared-" + UUID.randomUUID() + "@example.com";
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())", UUID.randomUUID(), email);
        int inserted = asOther(() -> jdbc.update("insert into customer (id, first_name, last_name, email, "
                + "password_hash, status, created_at) values (?, 'A', 'B', ?, 'x', 'ACTIVE', now())",
                UUID.randomUUID(), email));
        assertThat(inserted).isOne();
    }

    @Test
    void theLedgerCannotBeEditedByTheApplicationRole() {
        jdbc.update("insert into ledger_entry (id, customer_id, type, amount_minor, idempotency_key, created_at) "
                + "values (?, ?, 'TOPUP', 100, ?, now())", UUID.randomUUID(), gabonese, "k-" + UUID.randomUUID());
        assertThatThrownBy(() -> jdbc.update("update ledger_entry set amount_minor = 1 where customer_id = ?",
                gabonese)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("delete from ledger_entry where customer_id = ?", gabonese))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void aTokenFromAnotherCountryCannotReadThisCustomerOrSession() throws Exception {
        mvc.perform(get("/v1/me").with(TestTenants.customer(gabonese, OTHER)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v1/sessions/" + session).with(TestTenants.customer(gabonese, OTHER)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
        mvc.perform(get("/v1/sessions/" + session).with(TestTenants.customer(gabonese)))
                .andExpect(status().isOk());
    }

    @Test
    void tokensWithoutAValidTenantAreRejected() throws Exception {
        mvc.perform(get("/v1/me").with(jwt().jwt(token -> token.subject(gabonese.toString()))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mvc.perform(get("/v1/me").with(TestTenants.customer(gabonese, "ZZ")))
                .andExpect(status().isUnauthorized());
    }
}
