package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPagingIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    private String root;

    @BeforeEach
    void rootAdmin() {
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
    }

    private ResultActions list(String path, String query) throws Exception {
        return mvc.perform(get(path + "?" + query).header("Authorization", "Bearer " + root));
    }

    @Test
    void aHugeCustomerPageIsRefusedWithAStableCode() throws Exception {
        list("/v1/admin/customers", "page=2147483647&size=100").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAGE_OUT_OF_RANGE"));
    }

    @Test
    void aHugeAdminPageIsRefusedWithAStableCode() throws Exception {
        list("/v1/admin/users", "page=2147483647&size=100").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PAGE_OUT_OF_RANGE"));
    }

    @Test
    void ordinaryPagesStillWork() throws Exception {
        list("/v1/admin/customers", "page=0&size=20").andExpect(status().isOk());
        list("/v1/admin/users", "page=0&size=20").andExpect(status().isOk());
    }
}
