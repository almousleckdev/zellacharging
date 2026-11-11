package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.partner.PartnerFixtures;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminEvidenceTest extends PostgresTestBase {

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

    private AdminFixtures admin;
    private PartnerFixtures partner;
    private UUID helper;
    private String helperEmail;
    private String helperPhone;

    @BeforeEach
    void prepare() {
        admin = new AdminFixtures(admins, passwords, tokens);
        partner = new PartnerFixtures(jdbc);
        UUID suspended = partner.customer("GA", "Paul", "Moussavou", UUID.randomUUID() + "@example.com", null, "ACTIVE");
        partner.suspend("GA", suspended, 69000);
        helperEmail = UUID.randomUUID() + "@helper.example.com";
        helperPhone = "+2410" + (1000000 + java.util.concurrent.ThreadLocalRandom.current().nextInt(9000000));
        helper = partner.customer("GA", "Awa", "Mba", helperEmail, helperPhone, "ACTIVE");
        partner.evidence("GA", suspended, helper, false, 5000, "k-" + UUID.randomUUID());
        TenantContext.clear();
    }

    private ResultActions list(String token) throws Exception {
        return mvc.perform(get("/v1/admin/evidence?country=GA&size=100").header("Authorization", "Bearer " + token));
    }

    @Test
    void anAdminWithCustomersManageSeesTheReceivingAccountInFull() throws Exception {
        String token = admin.create(AdminRole.ADMIN, Set.of("GA")).accessToken();
        TenantContext.clear();

        list(token).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.receiving.id=='" + helper + "')].receiving.name").value("Awa Mba"))
                .andExpect(jsonPath("$.items[?(@.receiving.id=='" + helper + "')].receiving.phone").value(helperPhone))
                .andExpect(jsonPath("$.items[?(@.receiving.id=='" + helper + "')].receiving.email").value(helperEmail))
                .andExpect(jsonPath("$.items[?(@.receiving.id=='" + helper + "')].suspended.name").value("Paul Moussavou"));
    }

    @Test
    void aRoleWithoutCustomersManageIsRefused() throws Exception {
        String token = admin.create(AdminRole.SUPPORT, Set.of("GA")).accessToken();
        TenantContext.clear();

        list(token).andExpect(status().isForbidden());
    }
}
