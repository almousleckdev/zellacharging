package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountSelfServiceIntegrationTest extends PostgresTestBase {

    private static final String PASSWORD = "OldPass123";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PasswordHasher hasher;

    private UUID customer(long walletBalance) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, password_hash, status, created_at) "
                + "values (?, 'A', 'B', ?, ?, 'ACTIVE', now())", id, id + "@example.com", hasher.hash(PASSWORD));
        jdbc.update("insert into wallet (customer_id, currency, decimals, balance_minor, created_at) "
                + "values (?, 'XOF', 0, ?, now())", id, walletBalance);
        return id;
    }

    private ResultActions send(MockHttpServletRequestBuilder request, UUID id) throws Exception {
        return mvc.perform(request.with(jwt().jwt(j -> j.subject(id.toString()))));
    }

    private ResultActions json(MockHttpServletRequestBuilder request, UUID id, String body) throws Exception {
        return send(request.contentType(MediaType.APPLICATION_JSON).content(body), id);
    }

    @Test
    void theCurrentPasswordIsRequiredToChangeIt() throws Exception {
        UUID id = customer(0);

        json(post("/v1/me/password"), id, "{\"currentPassword\":\"wrong\",\"newPassword\":\"NewPass456\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));

        json(post("/v1/me/password"), id, "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"NewPass456\"}")
                .andExpect(status().isNoContent());

        String stored = jdbc.queryForObject("select password_hash from customer where id = ?", String.class, id);
        assertThat(hasher.matches("NewPass456", stored)).isTrue();
        assertThat(hasher.matches(PASSWORD, stored)).isFalse();
    }

    @Test
    void aTooShortNewPasswordIsRefused() throws Exception {
        UUID id = customer(0);

        json(post("/v1/me/password"), id, "{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"short\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void aProfilePictureCanBeUploadedReadAndRemoved() throws Exception {
        UUID id = customer(0);
        byte[] picture = {1, 2, 3, 4};

        send(put("/v1/me/avatar").contentType(MediaType.IMAGE_PNG).content(picture), id)
                .andExpect(status().isNoContent());
        send(get("/v1/me/avatar"), id).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(picture));
        send(delete("/v1/me/avatar"), id).andExpect(status().isNoContent());
        send(get("/v1/me/avatar"), id).andExpect(status().isNoContent());
    }

    @Test
    void onlyReasonablePicturesAreAccepted() throws Exception {
        UUID id = customer(0);

        send(put("/v1/me/avatar").contentType(MediaType.TEXT_PLAIN).content("hello"), id)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AVATAR"));
        send(put("/v1/me/avatar").contentType(MediaType.IMAGE_JPEG).content(new byte[1_000_001]), id)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_AVATAR"));
    }

    @Test
    void deletingTheAccountNeedsTheRightPassword() throws Exception {
        UUID id = customer(0);

        json(post("/v1/me/delete"), id, "{\"password\":\"wrong\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"));
    }

    @Test
    void anAccountWithMoneyInTheWalletCannotBeDeleted() throws Exception {
        UUID id = customer(5000);

        json(post("/v1/me/delete"), id, "{\"password\":\"" + PASSWORD + "\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_EMPTY"));
    }

    @Test
    void anAccountWithAChargeInProgressCannotBeDeleted() throws Exception {
        UUID id = customer(0);
        jdbc.update("insert into charging_session (id, customer_id, station_id, connector_id, id_tag, state, "
                + "created_at, updated_at) values (?, ?, 'CP-DEL', 1, 'x', 'ACTIVE', now(), now())",
                UUID.randomUUID(), id);

        json(post("/v1/me/delete"), id, "{\"password\":\"" + PASSWORD + "\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_STILL_ACTIVE"));
    }

    @Test
    void aClosedAccountLosesItsPersonalDataAndCannotBeUsedAgain() throws Exception {
        UUID id = customer(0);
        send(put("/v1/me/avatar").contentType(MediaType.IMAGE_PNG).content(new byte[] {9}), id);

        json(post("/v1/me/delete"), id, "{\"password\":\"" + PASSWORD + "\"}").andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("select status from customer where id = ?", String.class, id))
                .isEqualTo("DELETED");
        assertThat(jdbc.queryForObject("select email from customer where id = ?", String.class, id)).isNull();
        assertThat(jdbc.queryForObject("select count(*) from customer_avatar where customer_id = ?", Integer.class, id))
                .isZero();
        send(get("/v1/me"), id).andExpect(status().isNotFound());
    }

    @Test
    void theSupportContactsAreServedToTheApp() throws Exception {
        send(get("/v1/support"), customer(0)).andExpect(status().isOk()).andExpect(jsonPath("$.email").isEmpty());
    }
}
