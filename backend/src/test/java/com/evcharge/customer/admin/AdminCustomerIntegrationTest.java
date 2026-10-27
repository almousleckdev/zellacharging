package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCustomerIntegrationTest extends PostgresTestBase {

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

    private AdminFixtures fixtures;
    private AdminTestData data;
    private String marker;
    private String root;
    private String digits;

    @BeforeEach
    void prepare() {
        fixtures = new AdminFixtures(admins, passwords, tokens);
        data = new AdminTestData(jdbc);
        marker = "Zq" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        digits = String.valueOf(100000 + java.util.concurrent.ThreadLocalRandom.current().nextInt(900000));
        root = fixtures.create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
    }

    private ResultActions list(String token, String query) throws Exception {
        return mvc.perform(get("/v1/admin/customers?" + query).header("Authorization", "Bearer " + token));
    }

    private Instant ago(int minutes) {
        return Instant.now().minus(minutes, ChronoUnit.MINUTES);
    }

    @Test
    void searchMatchesNameEmailAndPhoneAndHonoursTheStatusFilter() throws Exception {
        UUID aline = data.customer("GA", "ACTIVE", marker + "Aline", "Obame", "+24177" + digits, ago(1));
        data.customer("GA", "BLOCKED", marker + "Brice", "Nzé", null, ago(2));

        list(root, "country=GA&q=" + marker + "aline").andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].id").value(aline.toString()));
        list(root, "country=GA&q=" + aline.toString().substring(0, 8)).andExpect(jsonPath("$.total").value(1));
        list(root, "country=GA&q=" + digits).andExpect(jsonPath("$.items[0].id").value(aline.toString()));
        list(root, "country=GA&q=" + marker + "&status=BLOCKED").andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].status").value("BLOCKED"));
    }

    @Test
    void searchTextIsTreatedLiterallyNotAsAPattern() throws Exception {
        data.customer("GA", "ACTIVE", marker + "Plain", "Name", null, ago(1));

        list(root, "country=GA&q=%25").andExpect(jsonPath("$.total").value(0));
        list(root, "country=GA&q=_").andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void pagesAreMergedNewestFirstAcrossCountries() throws Exception {
        data.customer("GA", "ACTIVE", marker + "One", "X", null, ago(5));
        data.customer("ML", "ACTIVE", marker + "Two", "X", null, ago(4));
        data.customer("GA", "ACTIVE", marker + "Three", "X", null, ago(3));
        data.customer("ML", "ACTIVE", marker + "Four", "X", null, ago(2));
        data.customer("GA", "ACTIVE", marker + "Five", "X", null, ago(1));

        list(root, "q=" + marker + "&size=2&page=0").andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.items[0].firstName").value(marker + "Five"))
                .andExpect(jsonPath("$.items[1].firstName").value(marker + "Four"))
                .andExpect(jsonPath("$.items[1].country").value("ML"));
        list(root, "q=" + marker + "&size=2&page=1").andExpect(jsonPath("$.items[0].firstName").value(marker + "Three"))
                .andExpect(jsonPath("$.items[1].firstName").value(marker + "Two"));
        list(root, "q=" + marker + "&size=2&page=2").andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].firstName").value(marker + "One"));
    }

    @Test
    void aCountryAdminOnlySeesItsOwnCountry() throws Exception {
        data.customer("GA", "ACTIVE", marker + "Gabonese", "X", null, ago(2));
        data.customer("ML", "ACTIVE", marker + "Malian", "X", null, ago(1));
        String gabon = fixtures.create(AdminRole.ADMIN, Set.of("GA")).accessToken();
        String mali = fixtures.create(AdminRole.ADMIN, Set.of("ML")).accessToken();

        list(gabon, "q=" + marker).andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].country").value("GA"));
        list(mali, "q=" + marker + "&country=ALL").andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].country").value("ML"));
        list(gabon, "q=" + marker + "&country=ML").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COUNTRY_FORBIDDEN"));
    }

    @Test
    void anAdminWithoutTheCustomerPermissionIsRefused() throws Exception {
        String operations = fixtures.create(AdminRole.OPERATIONS, Set.of("GA")).accessToken();

        list(operations, "country=GA").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PERMISSION_DENIED"));
    }

    @Test
    void detailShowsWalletAndSessionFiguresAndIsAudited() throws Exception {
        UUID customer = data.customer("GA", "ACTIVE", marker + "Detail", "Person", "+24162" + digits, ago(10));
        data.wallet("GA", customer, "XAF", 5000, 500);
        data.session("GA", customer, 1, "COMPLETED", "10.250", 1500L);
        data.topup("GA", customer, "SUCCEEDED", 8000);

        mvc.perform(get("/v1/admin/customers/GA/" + customer).header("Authorization", "Bearer " + root))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value(marker + "Detail"))
                .andExpect(jsonPath("$.wallet.balanceMinor").value(5000))
                .andExpect(jsonPath("$.wallet.topupsSucceededMinor").value(8000))
                .andExpect(jsonPath("$.sessions.completed").value(1))
                .andExpect(jsonPath("$.sessions.spentMinor").value(1500));

        assertThat(jdbc.queryForObject("select count(*) from admin_audit_record where action = 'CUSTOMER_VIEWED' "
                + "and subject = ?", Integer.class, customer.toString())).isEqualTo(1);
    }

    @Test
    void aCustomerOfAnotherCountryIsNotFoundAndAnUnscopedDetailIsRefused() throws Exception {
        UUID gabonese = data.customer("GA", "ACTIVE", marker + "Hidden", "Person", null, ago(1));

        mvc.perform(get("/v1/admin/customers/ML/" + gabonese).header("Authorization", "Bearer " + root))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
        mvc.perform(get("/v1/admin/customers/ALL/" + gabonese).header("Authorization", "Bearer " + root))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COUNTRY_REQUIRED"));
        String mali = fixtures.create(AdminRole.ADMIN, Set.of("ML")).accessToken();
        mvc.perform(get("/v1/admin/customers/GA/" + gabonese).header("Authorization", "Bearer " + mali))
                .andExpect(status().isForbidden());
    }
}
