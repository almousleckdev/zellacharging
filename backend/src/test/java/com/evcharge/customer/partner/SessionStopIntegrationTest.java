package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.charging.ChargeStopper;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SessionStopIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    @MockitoBean
    ChargeStopper stopper;

    private String key;

    @BeforeEach
    void prepare() {
        key = new PartnerFixtures(jdbc, keys).keyFor("GA");
    }

    @Test
    void thePartnerStopsACharge() throws Exception {
        mvc.perform(post("/v1/partners/ndzela/sessions/957/stop").header("X-Api-Key", key))
                .andExpect(status().isNoContent());

        verify(stopper).stopTransaction(957);
    }

    @Test
    void aMissingKeyIsRefused() throws Exception {
        mvc.perform(post("/v1/partners/ndzela/sessions/957/stop")).andExpect(status().isUnauthorized());
    }

    @Test
    void theReasonAChargeCannotBeStoppedIsReturned() throws Exception {
        doThrow(new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "No active charge to stop"))
                .when(stopper).stopTransaction(958);

        mvc.perform(post("/v1/partners/ndzela/sessions/958/stop").header("X-Api-Key", key))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SESSION_NOT_ACTIVE"));
    }
}
