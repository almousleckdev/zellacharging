package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.catalog.InventoryConnector;
import com.evcharge.customer.catalog.StationInventories;
import com.evcharge.customer.catalog.StationInventory;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminStationsIntegrationTest extends PostgresTestBase {

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

    private AdminTestData data;
    private String root;
    @MockitoBean
    StationInventories inventories;

    private String marker;

    @BeforeEach
    void prepare() {
        data = new AdminTestData(jdbc);
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        marker = "Zq" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        TenantContext.clear();
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + root));
    }

    @Test
    void stationsComeFromTheChargingPlatformJoinedWithOurActivityAndLimitedToTheAdminCountries() throws Exception {
        String station = "CP-" + marker;
        UUID customer = data.customer("GA", "ACTIVE", marker + "At", "Station", null, Instant.now());
        data.sessionAt("GA", customer, station, 1, "COMPLETED", "4.0", 700L);
        var gabon = new StationInventory(station, "GA", true, "ABB", "Terra", Instant.now(), null, null,
                List.of(new InventoryConnector(1, "Available", "CCS2", new java.math.BigDecimal("50"))));
        var mali = new StationInventory("ML-" + marker, "ML", false, null, null, null, null, null, List.of());
        when(inventories.all()).thenReturn(List.of(gabon, mali));
        when(inventories.find(station)).thenReturn(Optional.of(gabon));
        String gabonOnly = new AdminFixtures(admins, passwords, tokens).create(AdminRole.OPERATIONS, Set.of("GA"))
                .accessToken();

        mvc.perform(get("/v1/admin/stations?country=GA").header("Authorization", "Bearer " + root))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].station.id").value(station))
                .andExpect(jsonPath("$[0].station.vendor").value("ABB"))
                .andExpect(jsonPath("$[0].activity.sessions").value(1))
                .andExpect(jsonPath("$[0].activity.revenueMinor").value(700));
        mvc.perform(get("/v1/admin/stations").header("Authorization", "Bearer " + gabonOnly))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/v1/admin/stations/" + station).header("Authorization", "Bearer " + gabonOnly))
                .andExpect(status().isOk()).andExpect(jsonPath("$.station.connectors[0].status").value("Available"));
    }
}
