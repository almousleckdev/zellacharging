package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PayoutOutcome;
import com.evcharge.customer.wallet.domain.PayoutRequest;
import com.evcharge.customer.wallet.domain.PayoutStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PvitPayoutGatewayTest {

    private static final String REFERENCE = "WGAJBSWY3DPEHPK3PXP5";

    private MockRestServiceServer server;
    private PvitPayoutGateway gateway;

    @BeforeEach
    void wire() {
        PvitProperties properties = new PvitProperties("http://pvit.test", "ACC-1", "pw", "SECRETCODE",
                "PAYCODE", "STATUSCODE", "PAYOUTCODE", "CALLBACK", "ZELLA", "CHARGING", "CUSTOMER",
                Duration.ofSeconds(5));
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        JsonMapper mapper = JsonMapper.builder().build();
        gateway = new PvitPayoutGateway(new PvitApi(client, new PvitSecretProvider(client, properties, mapper),
                properties, mapper));
        server.expect(requestTo("http://pvit.test/v2/SECRETCODE/renew-secret")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"secret\":\"s3\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
    }

    private static PayoutRequest request() {
        return new PayoutRequest(REFERENCE, 4000, "XAF", MobileOperator.MOOV, "+24162820745");
    }

    private static String statusUrl() {
        return "http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                + "&accountOperationCode=ACC-1&transactionOperation=GIVE_CHANGE";
    }

    @Test
    void aPayoutUsesThePayoutCodeAndTheGiveChangeOperation() {
        server.expect(requestTo("http://pvit.test/v2/PAYOUTCODE/rest")).andExpect(header("X-Secret", "s3"))
                .andExpect(jsonPath("$.transaction_type").value("GIVE_CHANGE"))
                .andExpect(jsonPath("$.operator_code").value("MOOV_MONEY"))
                .andExpect(jsonPath("$.customer_account_number").value("062820745"))
                .andExpect(jsonPath("$.reference").value(REFERENCE))
                .andRespond(withSuccess("{\"status\":\"PENDING\"}", MediaType.APPLICATION_JSON));
        assertThat(gateway.payout(request()).status()).isEqualTo(PayoutStatus.PENDING);
        server.verify();
    }

    @Test
    void aRefusedPayoutFailsDefinitively() {
        server.expect(requestTo("http://pvit.test/v2/PAYOUTCODE/rest"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"CONSTRAINT_VIOLATION\"}"));
        PayoutOutcome outcome = gateway.payout(request());
        assertThat(outcome.status()).isEqualTo(PayoutStatus.FAILED);
        assertThat(outcome.failureCode()).isEqualTo("PROVIDER_REFUSED");
    }

    @Test
    void aServerErrorLeavesTheOutcomeUnknownSoTheMoneyIsNeverReversedBlindly() {
        server.expect(requestTo("http://pvit.test/v2/PAYOUTCODE/rest")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        assertThatThrownBy(() -> gateway.payout(request())).isInstanceOf(PvitException.class);
    }

    @Test
    void aConfirmedSuccessMarksThePayoutPaid() {
        server.expect(requestTo(statusUrl()))
                .andRespond(withSuccess("{\"status\":\"SUCCESS\",\"merchant_reference_id\":\"" + REFERENCE
                        + "\",\"reference_id\":\"p-1\"}", MediaType.APPLICATION_JSON));
        PayoutOutcome outcome = gateway.status(REFERENCE);
        assertThat(outcome.status()).isEqualTo(PayoutStatus.PAID);
        assertThat(outcome.providerRef()).isEqualTo("p-1");
    }

    @Test
    void anAnswerForAnotherReferenceIsNeverTrusted() {
        server.expect(requestTo(statusUrl()))
                .andRespond(withSuccess("{\"status\":\"SUCCESS\",\"merchant_reference_id\":\"OTHER\"}",
                        MediaType.APPLICATION_JSON));
        assertThat(gateway.status(REFERENCE).status()).isEqualTo(PayoutStatus.PENDING);
    }

    @Test
    void anUnknownTransactionIsStillPending() {
        server.expect(requestTo(statusUrl())).andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"TRANSACTION_NOT_FOUND\"}"));
        assertThat(gateway.status(REFERENCE).status()).isEqualTo(PayoutStatus.PENDING);
    }

    @Test
    void aDeclinedPayoutIsReportedAsFailed() {
        server.expect(requestTo(statusUrl()))
                .andRespond(withSuccess("{\"status\":\"FAILED\",\"merchant_reference_id\":\"" + REFERENCE + "\"}",
                        MediaType.APPLICATION_JSON));
        assertThat(gateway.status(REFERENCE).status()).isEqualTo(PayoutStatus.FAILED);
    }
}
