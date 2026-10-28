package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.application.TopupSettlement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentCallbackAcknowledgementTest {

    private MockMvc mvc;

    @BeforeEach
    void bind() {
        mvc = MockMvcBuilders.standaloneSetup(new PaymentCallbackController(Mockito.mock(TopupSettlement.class)))
                .build();
    }

    private ResultActions send(String body) throws Exception {
        return mvc.perform(post("/v1/payments/callback").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void aNumericCodeIsEchoedAsANumber() throws Exception {
        send("{\"transactionId\":\"PAY1\",\"merchantReferenceId\":\"GA1\",\"code\":200}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("PAY1"))
                .andExpect(jsonPath("$.responseCode").value(200));
    }

    @Test
    void aTextCodeIsEchoedAsText() throws Exception {
        send("{\"transactionId\":\"PAY2\",\"merchantReferenceId\":\"GA1\",\"code\":\"200\"}")
                .andExpect(jsonPath("$.responseCode").value("200"));
    }

    @Test
    void theEchoFollowsWhateverPvitSends() throws Exception {
        send("{\"transactionId\":\"PAY3\",\"merchantReferenceId\":\"GA1\",\"code\":401,\"status\":\"FAILED\"}")
                .andExpect(jsonPath("$.transactionId").value("PAY3"))
                .andExpect(jsonPath("$.responseCode").value(401));
    }
}
