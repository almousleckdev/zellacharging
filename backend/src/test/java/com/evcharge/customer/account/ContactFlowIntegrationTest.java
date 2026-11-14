package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.TestTenants;
import com.evcharge.customer.account.application.port.VerificationSender;
import com.evcharge.customer.account.domain.CodeDelivery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactFlowIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    VerificationSender sender;

    private UUID customer(String email, String phone) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into customer (id, first_name, last_name, email, phone_e164, password_hash, status, "
                + "created_at) values (?, 'Aline', 'Obame', ?, ?, 'x', 'ACTIVE', now())", id, email, phone);
        return id;
    }

    private String freshPhone() {
        return "+24177" + (100000 + ThreadLocalRandom.current().nextInt(900000));
    }

    private ResultActions submit(UUID id, String path, String json) throws Exception {
        return mvc.perform(post(path).with(TestTenants.customer(id)).contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String lastCode(String email) {
        return lastCode(CodeDelivery.EMAIL, email);
    }

    private String lastCode(CodeDelivery delivery, String target) {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(sender, atLeastOnce()).send(eq(delivery), eq(target), code.capture());
        return code.getValue();
    }

    @Test
    void anEmailTheCustomerAlreadyHasIsVerifiedWithACodeSentToIt() throws Exception {
        String email = "own-" + UUID.randomUUID() + "@example.com";
        UUID id = customer(email, freshPhone());
        submit(id, "/v1/me/email/code", "{}").andExpect(status().isNoContent());
        submit(id, "/v1/me/email/verify", "{\"code\":\"" + lastCode(email) + "\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(true));
        submit(id, "/v1/me/email/code", "{}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_VERIFIED"));
    }

    @Test
    void aCustomerWithOnlyAPhoneAddsAnEmailAndConfirmsItWithTheCodeSentThere() throws Exception {
        UUID id = customer(null, freshPhone());
        String email = "new-" + id + "@example.com";

        submit(id, "/v1/me/email", "{\"email\":\"" + email + "\"}").andExpect(status().isNoContent());
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.email").doesNotExist());

        submit(id, "/v1/me/email/verify", "{\"code\":\"" + lastCode(email) + "\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email)).andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void aWrongCodeLeavesTheEmailUnattached() throws Exception {
        UUID id = customer(null, freshPhone());
        submit(id, "/v1/me/email", "{\"email\":\"wrong-" + id + "@example.com\"}").andExpect(status().isNoContent());

        submit(id, "/v1/me/email/verify", "{\"code\":\"000000\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CODE"));
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void confirmingWithoutAnEmailWaitingIsRefused() throws Exception {
        UUID id = customer(null, freshPhone());

        submit(id, "/v1/me/email/verify", "{\"code\":\"123456\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NO_PENDING_EMAIL"));
    }

    @Test
    void anEmailAlreadyUsedIsRefusedWhetherAddingOrChanging() throws Exception {
        String taken = "taken-" + UUID.randomUUID() + "@example.com";
        customer(taken, null);
        UUID phoneOnly = customer(null, freshPhone());
        UUID withEmail = customer("own-" + UUID.randomUUID() + "@example.com", null);

        submit(phoneOnly, "/v1/me/email", "{\"email\":\"" + taken + "\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONTACT_ALREADY_USED"));
        submit(withEmail, "/v1/me/email", "{\"email\":\"own-again@example.com\"}")
                .andExpect(status().isNoContent());
    }

    @Test
    void aCustomerWithOnlyAnEmailAddsAPhoneAndConfirmsItWithTheWhatsAppCode() throws Exception {
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", null);
        String phone = freshPhone();

        submit(id, "/v1/me/phone", "{\"phone\":\"" + phone + "\",\"delivery\":\"WHATSAPP\"}").andExpect(status().isNoContent());
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.phone").doesNotExist());

        submit(id, "/v1/me/phone/verify", "{\"code\":\"" + lastCode(CodeDelivery.WHATSAPP, phone) + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.phoneVerified").value(true));
    }

    @Test
    void aCustomerChangesTheEmailByConfirmingTheCodeSentToTheNewOne() throws Exception {
        String old = "old-" + UUID.randomUUID() + "@example.com";
        UUID id = customer(old, freshPhone());
        String fresh = "fresh-" + id + "@example.com";

        submit(id, "/v1/me/email", "{\"email\":\"" + fresh + "\"}").andExpect(status().isNoContent());
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.email").value(old));

        submit(id, "/v1/me/email/verify", "{\"code\":\"" + lastCode(fresh) + "\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(fresh)).andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    void aCustomerChangesThePhoneByConfirmingTheCodeSentToTheNewNumber() throws Exception {
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", freshPhone());
        String fresh = freshPhone();

        submit(id, "/v1/me/phone", "{\"phone\":\"" + fresh + "\",\"delivery\":\"SMS\"}")
                .andExpect(status().isNoContent());

        submit(id, "/v1/me/phone/verify", "{\"code\":\"" + lastCode(CodeDelivery.SMS, fresh) + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.phone").value(fresh))
                .andExpect(jsonPath("$.phoneVerified").value(true));
    }

    @Test
    void aNumberAlreadyOnTheAccountIsVerifiedWithACodeReceivedBySms() throws Exception {
        String phone = freshPhone();
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", phone);

        submit(id, "/v1/me/phone/code", "{\"delivery\":\"SMS\"}").andExpect(status().isNoContent());
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.phoneVerified").value(false));

        submit(id, "/v1/me/phone/verify", "{\"code\":\"" + lastCode(CodeDelivery.SMS, phone) + "\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.phoneVerified").value(true));
        submit(id, "/v1/me/phone/code", "{\"delivery\":\"SMS\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHONE_ALREADY_VERIFIED"));
    }

    @Test
    void emailIsNotAChoiceForAPhoneCode() throws Exception {
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", freshPhone());

        submit(id, "/v1/me/phone/code", "{\"delivery\":\"EMAIL\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHANNEL_NOT_SUPPORTED"));
    }

    @Test
    void aWrongWhatsAppCodeLeavesThePhoneUnattached() throws Exception {
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", null);
        submit(id, "/v1/me/phone", "{\"phone\":\"" + freshPhone() + "\",\"delivery\":\"WHATSAPP\"}").andExpect(status().isNoContent());

        submit(id, "/v1/me/phone/verify", "{\"code\":\"000000\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CODE"));
        mvc.perform(get("/v1/me").with(TestTenants.customer(id))).andExpect(jsonPath("$.phone").doesNotExist());
    }

    @Test
    void confirmingWithoutAPhoneWaitingIsRefused() throws Exception {
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", null);

        submit(id, "/v1/me/phone/verify", "{\"code\":\"123456\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NO_PENDING_PHONE"));
    }

    @Test
    void aPhoneOfAnUnsupportedCountryOrAlreadyUsedOrInvalidIsRefused() throws Exception {
        String used = freshPhone();
        customer(null, used);
        UUID id = customer("mail-" + UUID.randomUUID() + "@example.com", null);

        submit(id, "/v1/me/phone", "{\"phone\":\"" + used + "\",\"delivery\":\"SMS\"}").andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONTACT_ALREADY_USED"));
        submit(id, "/v1/me/phone", "{\"phone\":\"+22370123456\",\"delivery\":\"SMS\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(anyOf(is("UNSUPPORTED_COUNTRY"), is("PHONE_COUNTRY_MISMATCH"))));
        submit(id, "/v1/me/phone", "{\"phone\":\"abc\",\"delivery\":\"SMS\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PHONE"));
    }
}
