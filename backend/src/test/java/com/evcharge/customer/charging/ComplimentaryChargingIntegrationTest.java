package com.evcharge.customer.charging;

import com.evcharge.customer.FakeOcppServer;
import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletFunding;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ComplimentaryChargingIntegrationTest extends PostgresTestBase {

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("POST", "/api/v1/id-tags", 201, "{\"idTag\":\"x\"}")
            .route("GET", "/api/v1/sessions", 200, "{\"items\":[]}");

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("zella.ocpp.base-url", OCPP::baseUrl);
    }

    @AfterAll
    static void stop() {
        OCPP.stop();
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    WalletFunding funding;

    private UUID emptyWallet() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, phone_e164, email_verified_at, "
                + "phone_verified_at, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, now(), now(), 'x', 'ACTIVE', now())", id, id + "@example.com",
                "+24177" + String.format("%07d", Math.abs(id.hashCode()) % 10000000));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, 0, now())", id);
        return id;
    }

    private void grant(UUID customer, Instant expiresAt) {
        jdbc.update("insert into customer_complimentary (customer_id, granted_by, reason, granted_at, expires_at) "
                + "values (?, ?, 'CEO', now(), ?)", customer, UUID.randomUUID(),
                expiresAt == null ? null : Timestamp.from(expiresAt));
    }

    private ResultActions start(UUID customer) throws Exception {
        String station = "CP-FREE" + SEQUENCE.incrementAndGet();
        OCPP.station(station).route("POST", "/api/v1/charge-points/" + station + "/connectors/1/start", 200,
                "{\"status\":\"PENDING\"}");
        return mvc.perform(post("/v1/sessions").with(TestTenants.customer(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stationId\":\"" + station + "\",\"connectorId\":1,\"amountMinor\":2000}"));
    }

    @Test
    void aCustomerWithoutFundsOrFreeAccessCannotStart() throws Exception {
        start(emptyWallet()).andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    void aCustomerWithFreeAccessStartsWithAnEmptyWalletAndNothingIsReserved() throws Exception {
        UUID customer = emptyWallet();
        grant(customer, null);

        start(customer).andExpect(status().isCreated());

        assertThat(jdbc.queryForObject("select reserved_minor from wallet where customer_id = ?", Long.class,
                customer)).isZero();
        assertThat(jdbc.queryForObject("select waived from funding_hold where customer_id = ?", Boolean.class,
                customer)).isTrue();
    }

    @Test
    void settlingAFreeChargeTakesNothingFromTheWalletAndWritesNoLedgerEntry() throws Exception {
        UUID customer = emptyWallet();
        grant(customer, null);
        start(customer).andExpect(status().isCreated());
        UUID session = jdbc.queryForObject("select session_id from funding_hold where customer_id = ?", UUID.class,
                customer);

        TenantContext.runAs(TENANT, () -> funding.settle(session, 1500L, "XOF"));

        assertThat(funding.waived(session)).isTrue();
        assertThat(jdbc.queryForObject("select balance_minor from wallet where customer_id = ?", Long.class,
                customer)).isZero();
        assertThat(jdbc.queryForObject("select captured_minor from funding_hold where session_id = ?", Long.class,
                session)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from ledger_entry where session_id = ?", Integer.class,
                session)).isZero();
    }

    @Test
    void theWalletTellsTheAppWhenChargingIsFree() throws Exception {
        UUID free = emptyWallet();
        grant(free, null);
        UUID paying = emptyWallet();

        mvc.perform(get("/v1/wallet").with(TestTenants.customer(free))).andExpect(status().isOk())
                .andExpect(jsonPath("$.freeCharging").value(true));
        mvc.perform(get("/v1/wallet").with(TestTenants.customer(paying))).andExpect(status().isOk())
                .andExpect(jsonPath("$.freeCharging").value(false));
    }

    @Test
    void anOlderAppThatOnlyReadsTheAvailableAmountCanStartAFreeCharge() throws Exception {
        UUID free = emptyWallet();
        grant(free, null);
        long allowance = funding.startLimits().maximumMinor();

        mvc.perform(get("/v1/wallet").with(TestTenants.customer(free))).andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMinor").value(allowance))
                .andExpect(jsonPath("$.balanceMinor").value(0))
                .andExpect(jsonPath("$.reservedMinor").value(0));
    }

    @Test
    void aPayingWalletStillShowsOnlyWhatItHolds() throws Exception {
        mvc.perform(get("/v1/wallet").with(TestTenants.customer(emptyWallet()))).andExpect(status().isOk())
                .andExpect(jsonPath("$.availableMinor").value(0))
                .andExpect(jsonPath("$.balanceMinor").value(0));
    }

    @Test
    void anExpiredFreeAccessNoLongerWaivesTheCharge() throws Exception {
        UUID customer = emptyWallet();
        grant(customer, Instant.now().minus(1, ChronoUnit.HOURS));

        start(customer).andExpect(status().isPaymentRequired());
    }
}
