package com.evcharge.customer.charging;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.partner.PartnerFixtures;
import com.evcharge.customer.partner.application.port.FreeChargingEligibility;
import com.evcharge.customer.shared.FreeSundayWindow;
import com.evcharge.customer.partner.application.port.NdzelaFreeCharging;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FreeSundayControllerTest extends PostgresTestBase {

    private static final String PATH = "/v1/free-sunday";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    NdzelaFreeCharging ndzela;

    private UUID linkedDriver() {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc);
        UUID customer = fixtures.customer("GA", "A", "B", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        fixtures.link("GA", customer);
        return customer;
    }

    @Test
    void unauthenticatedGetsUnauthorized() throws Exception {
        mvc.perform(get(PATH)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NO_AGREEMENT", "AGREEMENT_SUSPENDED", "OUTSIDE_WINDOW", "UNPAID_DAYS", "NOT_LINKED"})
    void reasonCodesAreReturnedUntranslated(String reason) throws Exception {
        UUID customer = linkedDriver();
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(false, reason, null, null, null));

        mvc.perform(get(PATH).with(TestTenants.customer(customer))).andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false)).andExpect(jsonPath("$.reason").value(reason))
                .andExpect(jsonPath("$.endsAt").doesNotExist());
    }

    @Test
    void anAvailableOfferCarriesTheEndOfTheWindow() throws Exception {
        Instant ends = Instant.parse("2026-11-22T14:00:00Z");
        UUID customer = linkedDriver();
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(),
                LocalDate.of(2026, 11, 22), ends));

        mvc.perform(get(PATH).with(TestTenants.customer(customer))).andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true)).andExpect(jsonPath("$.reason").value("ELIGIBLE"))
                .andExpect(jsonPath("$.endsAt").value("2026-11-22T14:00:00Z"));
    }

    @Test
    void theWindowIsReturnedSoTheAppShowsTheRealTimes() throws Exception {
        UUID customer = linkedDriver();
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(false, "OUTSIDE_WINDOW", null, null, null,
                new FreeSundayWindow("SUNDAY", "00:00", "23:59", true)));

        mvc.perform(get(PATH).with(TestTenants.customer(customer))).andExpect(status().isOk())
                .andExpect(jsonPath("$.window.day").value("SUNDAY")).andExpect(jsonPath("$.window.from").value("00:00"))
                .andExpect(jsonPath("$.window.until").value("23:59")).andExpect(jsonPath("$.window.endOfDay").value(true));
    }

    @Test
    void aCustomerOutsideTheProgramGetsNoWindow() throws Exception {
        mvc.perform(get(PATH).with(TestTenants.customer(UUID.randomUUID()))).andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("NOT_LINKED")).andExpect(jsonPath("$.window").doesNotExist());
    }

    @Test
    void ndzelaDownGivesUnavailableWithOk() throws Exception {
        UUID customer = linkedDriver();
        when(ndzela.check(customer)).thenReturn(FreeChargingEligibility.unavailable());

        mvc.perform(get(PATH).with(TestTenants.customer(customer))).andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false)).andExpect(jsonPath("$.reason").value("UNAVAILABLE"));
    }

    @Test
    void readingTheOfferNeverClaims() throws Exception {
        UUID customer = linkedDriver();
        when(ndzela.check(customer)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(),
                LocalDate.of(2026, 11, 22), Instant.now().plusSeconds(3600)));

        mvc.perform(get(PATH).with(TestTenants.customer(customer))).andExpect(status().isOk());

        assertThat(jdbc.queryForObject("select count(*) from free_sunday_claim where customer_id = ?",
                Integer.class, customer)).isZero();
    }

    @Test
    void anUnlinkedCustomerGetsNotLinkedAndNdzelaIsNeverCalled() throws Exception {
        mvc.perform(get(PATH).with(TestTenants.anyCustomer())).andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false)).andExpect(jsonPath("$.reason").value("NOT_LINKED"));

        verifyNoInteractions(ndzela);
    }

    @Test
    void aCustomerNeverReceivesTheOfferOfAnotherCustomer() throws Exception {
        UUID first = linkedDriver();
        UUID second = linkedDriver();
        when(ndzela.check(first)).thenReturn(new FreeChargingEligibility(false, "UNPAID_DAYS", null, null, null));
        when(ndzela.check(second)).thenReturn(new FreeChargingEligibility(true, "ELIGIBLE", UUID.randomUUID(),
                LocalDate.of(2026, 11, 22), Instant.parse("2026-11-22T14:00:00Z")));

        mvc.perform(get(PATH).with(TestTenants.customer(first))).andExpect(jsonPath("$.reason").value("UNPAID_DAYS"));
        mvc.perform(get(PATH).with(TestTenants.customer(second))).andExpect(jsonPath("$.reason").value("ELIGIBLE"));
    }
}
