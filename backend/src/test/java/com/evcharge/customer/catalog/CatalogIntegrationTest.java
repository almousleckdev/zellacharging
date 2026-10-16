package com.evcharge.customer.catalog;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.FakeOcppServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogIntegrationTest extends PostgresTestBase {

    private static final String STATION = """
            {"id":"CP-1","isoCode":"GA","vendor":"JC.DW","model":"V1","online":true,"lastSeenAt":null,
             "latitude":0.4162,"longitude":9.4673,"unknownField":1,
             "connectors":[{"connectorId":1,"status":"Available","connectorType":"GBT_DC","maxPowerKw":60.0},
                           {"connectorId":2,"status":"Charging","connectorType":"GBT_DC","maxPowerKw":60.0}]}
            """;

    private static final FakeOcppServer OCPP = new FakeOcppServer()
            .route("GET", "/api/v1/charge-points", 200, "[" + STATION + "]")
            .route("GET", "/api/v1/charge-points/CP-1", 200, STATION)
            .route("GET", "/c/good-token", 200,
                    "{\"deviceId\":\"CP-1\",\"connectorId\":1,\"status\":\"Available\"}");

    @DynamicPropertySource
    static void ocpp(DynamicPropertyRegistry registry) {
        registry.add("ndzela.ocpp.base-url", OCPP::baseUrl);
    }

    @AfterAll
    static void stop() {
        OCPP.stop();
    }

    @Autowired
    MockMvc mvc;

    @Test
    void stationsRequireAuthentication() throws Exception {
        mvc.perform(get("/v1/stations")).andExpect(status().isUnauthorized());
    }

    @Test
    void listsStationsWithAvailabilityAndDistance() throws Exception {
        mvc.perform(get("/v1/stations").param("lat", "0.39").param("lng", "9.45").param("radiusKm", "10")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CP-1"))
                .andExpect(jsonPath("$[0].availableConnectors").value(1))
                .andExpect(jsonPath("$[0].totalConnectors").value(2))
                .andExpect(jsonPath("$[0].distanceKm").isNumber());
    }

    @Test
    void rejectsPartialLocation() throws Exception {
        mvc.perform(get("/v1/stations").param("lat", "0.39").with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_LOCATION"));
    }

    @Test
    void resolvesScannedUrlAndPlainToken() throws Exception {
        for (String content : new String[] {"https://api.example.com/c/good-token", "good-token"}) {
            mvc.perform(post("/v1/qr/resolve").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"" + content + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.station.id").value("CP-1"))
                    .andExpect(jsonPath("$.connector.connectorId").value(1))
                    .andExpect(jsonPath("$.connector.available").value(true));
        }
    }

    @Test
    void unknownQrIsNotFoundWithStableCode() throws Exception {
        mvc.perform(post("/v1/qr/resolve").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"https://x/c/bad\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVALID_QR"));
    }
}
