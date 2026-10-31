package com.evcharge.customer.account;

import com.evcharge.customer.PostgresTestBase;
import com.evcharge.customer.account.application.port.ContactInbox;
import com.evcharge.customer.account.domain.ContactMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactMessageIntegrationTest extends PostgresTestBase {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ContactInbox inbox;

    private ResultActions send(String email, String message) throws Exception {
        return mvc.perform(post("/v1/contact").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Aline Obame","email":"%s","topic":"Paiement","message":"%s"}
                """.formatted(email, message)));
    }

    @Test
    void anyoneCanSendAMessageWithoutSigningIn() throws Exception {
        when(inbox.deliver(any())).thenReturn(true);

        send("aline@example.com", "Mon paiement est bloque depuis hier.").andExpect(status().isAccepted());

        ArgumentCaptor<ContactMessage> delivered = ArgumentCaptor.forClass(ContactMessage.class);
        verify(inbox).deliver(delivered.capture());
        assertThat(delivered.getValue().email()).isEqualTo("aline@example.com");
        assertThat(delivered.getValue().phone()).isNull();
    }

    @Test
    void anInvalidEmailOrAShortMessageIsRefused() throws Exception {
        send("not-an-email", "Mon paiement est bloque depuis hier.").andExpect(status().isBadRequest());
        send("aline@example.com", "short").andExpect(status().isBadRequest());
    }

    @Test
    void aMessageThatCannotBeDeliveredIsReported() throws Exception {
        when(inbox.deliver(any())).thenReturn(false);

        send("aline@example.com", "Mon paiement est bloque depuis hier.").andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CONTACT_UNAVAILABLE"));
    }
}
