package com.evcharge.customer.shared;

import com.evcharge.customer.TestTenants;
import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    private ResultActions asCustomer(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.with(TestTenants.customer(UUID.randomUUID())));
    }

    @Test
    void anonymousCallsAreRejectedWithAStableCodeInFrenchByDefault() throws Exception {
        mvc.perform(get("/v1/sessions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.detail").value("Veuillez vous connecter pour continuer."))
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }

    @Test
    void theMessageFollowsTheAcceptLanguageHeader() throws Exception {
        mvc.perform(get("/v1/sessions").header("Accept-Language", "en"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Please sign in to continue."));
    }

    @Test
    void anUnsupportedLanguageFallsBackToFrench() throws Exception {
        mvc.perform(get("/v1/sessions").header("Accept-Language", "de"))
                .andExpect(jsonPath("$.detail").value("Veuillez vous connecter pour continuer."));
    }

    @Test
    void aSuppliedCorrelationIdIsEchoedAndAnUnsafeOneIsReplaced() throws Exception {
        mvc.perform(get("/v1/sessions").header("X-Correlation-Id", "support-ticket-4821"))
                .andExpect(header().string("X-Correlation-Id", "support-ticket-4821"))
                .andExpect(jsonPath("$.correlationId").value("support-ticket-4821"));
        String replaced = mvc.perform(get("/v1/sessions").header("X-Correlation-Id", "bad id\r\n"))
                .andReturn().getResponse().getHeader("X-Correlation-Id");
        assertThat(replaced).matches("[0-9a-f-]{36}");
    }

    @Test
    void aMalformedPathIdentifierIsABadRequestNotAServerError() throws Exception {
        asCustomer(get("/v1/sessions/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void unknownRoutesAndMethodsKeepTheCommonShape() throws Exception {
        asCustomer(get("/v1/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        asCustomer(put("/v1/sessions"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void malformedJsonAndInvalidFieldsAreDistinguished() throws Exception {
        asCustomer(post("/v1/sessions").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        asCustomer(post("/v1/sessions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"stationId\":\"\",\"connectorId\":0,\"amountMinor\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields.stationId").exists());
    }

    @Test
    void failureBodiesNeverLeakInternals() throws Exception {
        String body = asCustomer(get("/v1/sessions/not-a-uuid")).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("Exception", "java.", "org.springframework", "UUID");
    }
}
