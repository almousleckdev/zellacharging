package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.charging.application.FeeRates;
import com.evcharge.customer.charging.application.PriceList;
import com.evcharge.customer.wallet.application.WalletSettings;
import com.evcharge.customer.wallet.application.WithdrawalSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSettingsIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @Autowired
    WalletSettings wallet;

    @Autowired
    WithdrawalSettings withdrawals;

    @Autowired
    FeeRates fees;

    @Autowired
    PriceList prices;

    private AdminFixtures fixtures;
    private String root;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
    }

    @AfterEach
    void restoreDefaults() throws Exception {
        save(root, "wallet", "{\"values\":{\"minimumStartMinor\":null,\"maximumStartMinor\":null,\"minTopupMinor\":null,"
                + "\"maxTopupMinor\":null,\"withdrawalsEnabled\":null,\"withdrawalMinimumMinor\":null,"
                + "\"withdrawalMaximumMinor\":null,\"withdrawalDailyMaximumMinor\":null}}");
        save(root, "charging", "{\"values\":{\"pricePerKwhMinor\":null,\"serviceFeePercent\":null,\"vatPercent\":null}}");
    }

    private ResultActions save(String token, String section, String body) throws Exception {
        return mvc.perform(put("/v1/admin/settings/" + section).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void theSuperAdminChangesTheWalletLimitsAndTheServerUsesThemAtOnce() throws Exception {
        save(root, "wallet", "{\"values\":{\"minTopupMinor\":1000,\"withdrawalMaximumMinor\":300000,"
                + "\"withdrawalsEnabled\":true}}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.section").value("wallet"))
                .andExpect(jsonPath("$.values.minTopupMinor").value(1000))
                .andExpect(jsonPath("$.values.withdrawalsEnabled").value(true));

        assertThat(wallet.minTopupMinor()).isEqualTo(1000);
        assertThat(withdrawals.maximumMinor()).isEqualTo(300000);
        assertThat(withdrawals.enabled()).isTrue();
        assertThat(jdbc.queryForObject("select count(*) from admin_audit_record where action = 'SETTING_UPDATED' "
                + "and subject = 'wallet.minTopupMinor'", Integer.class)).isPositive();
    }

    @Test
    void thePriceAndTheRatesAreEditableAndCanBeRestored() throws Exception {
        save(root, "charging", "{\"values\":{\"pricePerKwhMinor\":250,\"serviceFeePercent\":3.5,\"vatPercent\":18}}")
                .andExpect(status().isOk());

        assertThat(prices.pricePerKwhMinor()).isEqualTo(250L);
        assertThat(fees.serviceFeePercent()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(fees.vatPercent()).isEqualByComparingTo(new BigDecimal("18"));

        save(root, "charging", "{\"values\":{\"vatPercent\":null}}").andExpect(status().isOk());

        assertThat(fees.vatPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void invalidValuesAreRefusedAndChangeNothing() throws Exception {
        save(root, "wallet", "{\"values\":{\"minTopupMinor\":-5}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SETTING"));
        save(root, "wallet", "{\"values\":{\"minTopupMinor\":\"abc\"}}").andExpect(status().isBadRequest());
        save(root, "wallet", "{\"values\":{\"paymentProvider\":\"x\"}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SETTING"));
        save(root, "charging", "{\"values\":{\"serviceFeePercent\":101}}").andExpect(status().isBadRequest());
        save(root, "wallet", "{\"values\":{\"minTopupMinor\":600000}}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SETTING_RANGE_INVALID"));

        assertThat(wallet.minTopupMinor()).isEqualTo(100);
    }

    @Test
    void anUnknownSectionAnswersNotFound() throws Exception {
        save(root, "nothing", "{\"values\":{\"a\":1}}").andExpect(status().isNotFound());
    }

    @Test
    void onlyTheSuperAdminMayEdit() throws Exception {
        String admin = fixtures.create(AdminRole.ADMIN, Set.of("GA")).accessToken();

        save(admin, "wallet", "{\"values\":{\"minTopupMinor\":1000}}").andExpect(status().isForbidden());

        assertThat(wallet.minTopupMinor()).isEqualTo(100);
    }
}
