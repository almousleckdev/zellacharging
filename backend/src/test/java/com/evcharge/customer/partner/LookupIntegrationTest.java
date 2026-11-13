package com.evcharge.customer.partner;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.shared.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LookupIntegrationTest extends PostgresTestBase {

    private static final String PATH = "/v1/partners/ndzela/id-tags/lookup";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    PartnerKeys keys;

    private PartnerFixtures fixtures;
    private String key;

    @BeforeEach
    void prepare() {
        fixtures = new PartnerFixtures(jdbc, keys);
        key = fixtures.keyFor("GA");
    }

    private static String email() {
        return "d" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "@example.com";
    }

    private static String phone() {
        return "+2416" + String.format("%07d", (int) (Math.random() * 10_000_000));
    }

    private static String driver(String ref, String email, String phone) {
        return "{\"ref\":\"" + ref + "\",\"email\":\"" + email + "\",\"phoneE164\":\"" + phone + "\"}";
    }

    private ResultActions lookup(String... drivers) throws Exception {
        return lookupBody("{\"drivers\":[" + String.join(",", drivers) + "]}");
    }

    private ResultActions lookupBody(String body) throws Exception {
        return mvc.perform(post(PATH).header("X-Api-Key", key).contentType("application/json").content(body));
    }

    @Test
    void fullMatchReturnsNamesAndBothFields() throws Exception {
        String email = email();
        String phone = phone();
        UUID id = fixtures.customer("GA", "Ada", "Lovelace", email, phone, "ACTIVE");
        String tag = fixtures.idTagFor("GA", id);

        lookup(driver("d1", email, phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].ref").value("d1"))
                .andExpect(jsonPath("$.results[0].matches[0].customerId").value(id.toString()))
                .andExpect(jsonPath("$.results[0].matches[0].idTag").value(tag))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("FULL"))
                .andExpect(jsonPath("$.results[0].matches[0].matchedOn[0]").value("email"))
                .andExpect(jsonPath("$.results[0].matches[0].matchedOn[1]").value("phone"))
                .andExpect(jsonPath("$.results[0].matches[0].firstName").value("Ada"))
                .andExpect(jsonPath("$.results[0].matches[0].lastName").value("Lovelace"));
    }

    @Test
    void aGoogleOrAppleCustomerWithAProvenEmailIsAFullMatchWithoutAPhone() throws Exception {
        String email = email();
        UUID id = fixtures.customer("GA", "Ada", "Lovelace", email, null, "ACTIVE");
        fixtures.signedInWithGoogle("GA", id);

        lookup(driver("d1", email, phone())).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(1)))
                .andExpect(jsonPath("$.results[0].matches[0].customerId").value(id.toString()))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("FULL"))
                .andExpect(jsonPath("$.results[0].matches[0].matchedOn[0]").value("email"))
                .andExpect(jsonPath("$.results[0].matches[0].firstName").value("Ada"));
    }

    @Test
    void aProvenSocialEmailIsFullEvenWhenAnotherAccountMatchesThePhone() throws Exception {
        String email = email();
        String phone = phone();
        UUID social = fixtures.customer("GA", "Ada", "Lovelace", email, null, "ACTIVE");
        fixtures.signedInWithGoogle("GA", social);
        UUID byPhone = fixtures.customer("GA", "Ada", "L", email(), phone, "ACTIVE");

        lookup(driver("d1", email, phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(2)))
                .andExpect(jsonPath("$.results[0].matches[0].customerId").value(social.toString()))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("FULL"))
                .andExpect(jsonPath("$.results[0].matches[1].customerId").value(byPhone.toString()))
                .andExpect(jsonPath("$.results[0].matches[1].quality").value("PARTIAL"));
    }

    @Test
    void emailOnlyIsPartialWithoutNames() throws Exception {
        String email = email();
        UUID id = fixtures.customer("GA", "Ada", "Lovelace", email, phone(), "ACTIVE");

        lookup(driver("d1", email, phone())).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(1)))
                .andExpect(jsonPath("$.results[0].matches[0].customerId").value(id.toString()))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("PARTIAL"))
                .andExpect(jsonPath("$.results[0].matches[0].matchedOn[0]").value("email"))
                .andExpect(jsonPath("$.results[0].matches[0].firstName").doesNotExist())
                .andExpect(jsonPath("$.results[0].matches[0].lastName").doesNotExist());
    }

    @Test
    void phoneOnlyIsPartialWithoutNames() throws Exception {
        String phone = phone();
        fixtures.customer("GA", "Ada", "Lovelace", email(), phone, "ACTIVE");

        lookup(driver("d1", email(), phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("PARTIAL"))
                .andExpect(jsonPath("$.results[0].matches[0].matchedOn[0]").value("phone"))
                .andExpect(jsonPath("$.results[0].matches[0].firstName").doesNotExist());
    }

    @Test
    void splitMatchGivesTwoPartialsForOneRef() throws Exception {
        String email = email();
        String phone = phone();
        UUID byEmail = fixtures.customer("GA", "A", "One", email, phone(), "ACTIVE");
        UUID byPhone = fixtures.customer("GA", "B", "Two", email(), phone, "ACTIVE");

        String body = lookup(driver("d1", email, phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(2)))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("PARTIAL"))
                .andExpect(jsonPath("$.results[0].matches[1].quality").value("PARTIAL"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains(byEmail.toString()).contains(byPhone.toString())
                .doesNotContain("\"One\"").doesNotContain("\"Two\"");
    }

    @Test
    void noMatchReturnsEmptyMatchesAndNothingElse() throws Exception {
        String body = lookup(driver("d1", email(), phone())).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].ref").value("d1"))
                .andExpect(jsonPath("$.results[0].matches", hasSize(0))).andReturn().getResponse()
                .getContentAsString();

        assertThat(body).doesNotContain("customerId").doesNotContain("firstName");
    }

    @Test
    void refIsEchoedPerEntryAndDuplicateEmailsAnswerEachRefIndependently() throws Exception {
        String email = email();
        String phone = phone();
        fixtures.customer("GA", "Ada", "Lovelace", email, phone, "ACTIVE");

        lookup(driver("first", email, phone), driver("second", email, phone), driver("third", email(), phone()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].ref").value("first"))
                .andExpect(jsonPath("$.results[0].matches[0].quality").value("FULL"))
                .andExpect(jsonPath("$.results[1].ref").value("second"))
                .andExpect(jsonPath("$.results[1].matches[0].quality").value("FULL"))
                .andExpect(jsonPath("$.results[2].ref").value("third"))
                .andExpect(jsonPath("$.results[2].matches", hasSize(0)));
    }

    @Test
    void batchOf201IsRefusedAndEmptyBatchIsRefused() throws Exception {
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 201; i++) {
            many.add(driver("d" + i, email(), phone()));
        }

        lookup(many.toArray(String[]::new)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_BATCH_TOO_LARGE"));
        lookup().andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("PARTNER_BATCH_EMPTY"));
    }

    @Test
    void batchOf200IsAccepted() throws Exception {
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            many.add(driver("d" + i, email(), phone()));
        }

        lookup(many.toArray(String[]::new)).andExpect(status().isOk()).andExpect(jsonPath("$.results", hasSize(200)));
    }

    @Test
    void malformedContactsAreRefusedNotSilentlyUnmatched() throws Exception {
        lookup(driver("d1", "Upper@Example.com", phone())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CONTACT_INVALID"));
        lookup(driver("d1", " spaced@example.com", phone())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CONTACT_INVALID"));
        lookup(driver("d1", email(), "+241 77 00 00 00")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CONTACT_INVALID"));
        lookup(driver("d1", email(), "077000000")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CONTACT_INVALID"));
        lookup(driver("", email(), phone())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PARTNER_CONTACT_INVALID"));
    }

    @Test
    void idTagIsNullBeforeTheFirstChargeAndLookupCreatesNone() throws Exception {
        String email = email();
        String phone = phone();
        fixtures.customer("GA", "Ada", "Lovelace", email, phone, "ACTIVE");
        Integer before = jdbc.queryForObject("select count(*) from customer_id_tag", Integer.class);

        lookup(driver("d1", email, phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches[0].idTag").value(nullValue()));

        assertThat(jdbc.queryForObject("select count(*) from customer_id_tag", Integer.class)).isEqualTo(before);
    }

    @Test
    void blockedAndPendingCustomersAreNeverMatched() throws Exception {
        String blocked = email();
        String pendingPhone = phone();
        fixtures.customer("GA", "A", "B", blocked, null, "BLOCKED");
        fixtures.customer("GA", "A", "B", email(), pendingPhone, "PENDING_PHONE");

        lookup(driver("d1", blocked, phone()), driver("d2", email(), pendingPhone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(0)))
                .andExpect(jsonPath("$.results[1].matches", hasSize(0)));
    }

    @Test
    void customersOfAnotherCountryAreNeverMatched() throws Exception {
        String email = email();
        String phone = phone();
        fixtures.customer("ML", "A", "B", email, phone, "ACTIVE");

        lookup(driver("d1", email, phone)).andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].matches", hasSize(0)));
    }

    @Test
    void auditRowHoldsCountsOnlyAndNoEmailOrPhone() throws Exception {
        String email = email();
        String phone = phone();
        fixtures.customer("GA", "Ada", "Lovelace", email, phone, "ACTIVE");
        lookup(driver("d1", email, phone), driver("d2", email(), phone())).andExpect(status().isOk());

        String detail = TenantContext.callAs("GA", () -> jdbc.queryForObject("select detail from audit_record "
                + "where action = 'PARTNER_LOOKUP' order by created_at desc limit 1", String.class));

        assertThat(detail).contains("entries=2").contains("full=1").contains("partial=0")
                .doesNotContain(email).doesNotContain(phone).doesNotContain("Ada");
    }

    @Test
    void lookupWithoutKeyIs401() throws Exception {
        mvc.perform(post(PATH).contentType("application/json").content("{\"drivers\":[]}"))
                .andExpect(status().isUnauthorized());
    }
}
