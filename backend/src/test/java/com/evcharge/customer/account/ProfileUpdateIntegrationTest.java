package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileUpdateIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private UUID customer() {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'Old', 'Name', ?, 'x', 'ACTIVE', now())", id, id + "@example.com");
        return id;
    }

    @Test
    void aCustomerCanChangeTheirName() throws Exception {
        UUID id = customer();

        mvc.perform(patch("/v1/me").with(jwt().jwt(j -> j.subject(id.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"  Marie \",\"lastName\":\"Obame\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.lastName").value("Obame"));

        mvc.perform(get("/v1/me").with(jwt().jwt(j -> j.subject(id.toString()))))
                .andExpect(jsonPath("$.firstName").value("Marie"));
    }

    @Test
    void aBlankNameIsRefused() throws Exception {
        UUID id = customer();

        mvc.perform(patch("/v1/me").with(jwt().jwt(j -> j.subject(id.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\" \",\"lastName\":\"Obame\"}"))
                .andExpect(status().isBadRequest());
    }
}
