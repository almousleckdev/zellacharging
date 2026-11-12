package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.partner.PartnerFixtures;
import com.evcharge.customer.partner.PartnerKeys;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuspendedSessionIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    @Autowired
    PartnerKeys keys;

    private record Session(UUID customer, String accessToken, String refreshToken, String partnerKey) {
    }

    private Session aSignedInCustomer() throws Exception {
        PartnerFixtures fixtures = new PartnerFixtures(jdbc, keys);
        String email = UUID.randomUUID() + "@example.com";
        UUID customer = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, email_verified_at, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, now(), ?, 'ACTIVE', now())", customer, email, hasher.hash("Secret123!"));
        fixtures.link(TENANT, customer);
        String body = mvc.perform(post("/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + email + "\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return new Session(customer, JsonPath.read(body, "$.accessToken"), JsonPath.read(body, "$.refreshToken"),
                fixtures.keyFor(TENANT));
    }

    private void suspend(Session session) throws Exception {
        mvc.perform(put("/v1/partners/ndzela/customers/" + session.customer() + "/suspension")
                        .header("X-Api-Key", session.partnerKey()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"owedMinor\":69000,\"currency\":\"XAF\",\"decimals\":0}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void theNextRequestOfASuspendedDriverIsRefusedAsSuspendedWithTheAmountOwedNotAsSignedInElsewhere() throws Exception {
        Session session = aSignedInCustomer();
        mvc.perform(get("/v1/wallet").header("Authorization", "Bearer " + session.accessToken())).andExpect(status().isOk());

        suspend(session);

        mvc.perform(get("/v1/wallet").header("Authorization", "Bearer " + session.accessToken()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.owedMinor").value(69000))
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void refreshingAfterTheSuspensionSaysSuspendedWithTheAmountOwedInsteadOfAnInvalidToken() throws Exception {
        Session session = aSignedInCustomer();
        suspend(session);

        mvc.perform(post("/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + session.refreshToken() + "\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"))
                .andExpect(jsonPath("$.owedMinor").value(69000));
    }

    @Test
    void anUnknownRefreshTokenIsStillJustInvalid() throws Exception {
        mvc.perform(post("/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }
}
