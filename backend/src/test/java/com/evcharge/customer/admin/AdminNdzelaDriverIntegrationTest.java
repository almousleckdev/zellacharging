package com.evcharge.customer.admin;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.partner.NdzelaDriver;
import com.evcharge.customer.partner.NdzelaDrivers;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminNdzelaDriverIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    AdminRepository admins;

    @Autowired
    AdminPasswordHasher passwords;

    @Autowired
    AdminTokenIssuer tokens;

    @MockitoBean
    NdzelaDrivers ndzela;

    private String root;
    private UUID customer;

    @BeforeEach
    void prepare() throws Exception {
        root = new AdminFixtures(admins, passwords, tokens).create(AdminRole.SUPER_ADMIN, Set.of()).accessToken();
        TenantContext.clear();
        String json = call(post("/v1/admin/customers/GA").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Ada\",\"lastName\":\"Obiang\",\"email\":\"ada." + UUID.randomUUID()
                        + "@example.com\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        customer = UUID.fromString(json.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
    }

    private ResultActions call(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + root));
    }

    private String path() {
        return "/v1/admin/customers/GA/" + customer + "/ndzela-driver";
    }

    private void link() throws Exception {
        call(put("/v1/admin/customers/GA/" + customer + "/ndzela-link")).andExpect(status().isNoContent());
    }

    @Test
    void aCustomerNotLinkedToNdzelaHasNoDriverAndNdzelaIsNotAsked() throws Exception {
        call(get(path())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NDZELA_NOT_LINKED"));
    }

    @Test
    void aLinkedCustomerShowsTheContractAndTheCarNdzelaHolds() throws Exception {
        link();
        when(ndzela.find(customer)).thenReturn(Optional.of(new NdzelaDriver(
                new NdzelaDriver.Person("Ada", "Obiang", "+24106555111", "ada.nd@example.com", "ACTIVE"),
                new NdzelaDriver.Contract("NDZ-GA-0042", "ACTIVE", LocalDate.of(2026, 11, 1),
                        LocalDate.of(2027, 5, 1), new BigDecimal("23000")),
                new NdzelaDriver.Car("V001", "GA-123-AB", "BYD", "Dolphin", 2025, "Blanc"))));

        call(get(path())).andExpect(status().isOk())
                .andExpect(jsonPath("$.contract.number").value("NDZ-GA-0042"))
                .andExpect(jsonPath("$.car.fleetNumber").value("V001"))
                .andExpect(jsonPath("$.car.plate").value("GA-123-AB"));
    }

    @Test
    void ndzelaWithoutADriverForTheCustomerIsNotFound() throws Exception {
        link();
        when(ndzela.find(any())).thenReturn(Optional.empty());

        call(get(path())).andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NDZELA_DRIVER_NOT_FOUND"));
    }

    @Test
    void ndzelaBeingDownIsReportedAsUnavailable() throws Exception {
        link();
        when(ndzela.find(any())).thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "NDZELA_UNAVAILABLE", "down"));

        call(get(path())).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("NDZELA_UNAVAILABLE"));
    }
}
