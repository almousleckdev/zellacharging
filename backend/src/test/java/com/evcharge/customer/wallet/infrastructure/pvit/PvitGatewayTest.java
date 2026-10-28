package com.evcharge.customer.wallet.infrastructure.pvit;

import com.evcharge.customer.wallet.domain.CollectionRequest;
import com.evcharge.customer.wallet.domain.MobileOperator;
import com.evcharge.customer.wallet.domain.PaymentOutcome;
import com.evcharge.customer.wallet.domain.TopupState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
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
import org.springframework.http.HttpStatus;

class PvitGatewayTest {

    private static final String REFERENCE = "GAJBSWY3DPEHPK3PXP5X";

    private MockRestServiceServer server;
    private PvitGateway gateway;

    @BeforeEach
    void wire() {
        PvitProperties properties = new PvitProperties("http://pvit.test", "ACC-1", null, "pw",
                "SECRETCODE", "PAYCODE", "STATUSCODE", "PAYOUTCODE", "", "CALLBACK", "ZELLA", "CHARGING", "CUSTOMER",
                Duration.ofSeconds(5));
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        JsonMapper mapper = JsonMapper.builder().build();
        PvitSecretProvider secrets = new PvitSecretProvider(client, properties, "ACC-1", mapper);
        gateway = new PvitGateway(MobileOperator.AIRTEL, new PvitApi(client, secrets, properties, "ACC-1", mapper),
                new PvitKyc(client, secrets, properties, mapper));
        server.expect(requestTo("http://pvit.test/v2/SECRETCODE/renew-secret")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"secret\":\"s3\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
    }

    private static CollectionRequest request() {
        return new CollectionRequest(REFERENCE, 5000, "XAF", MobileOperator.AIRTEL, "+24177123456");
    }

    @Test
    void startingACollectionSendsTheNationalNumberAndStaysPending() {
        server.expect(requestTo("http://pvit.test/v2/PAYCODE/rest")).andExpect(header("X-Secret", "s3"))
                .andExpect(jsonPath("$.operator_code").value("AIRTEL_MONEY"))
                .andExpect(jsonPath("$.customer_account_number").value("077123456"))
                .andExpect(jsonPath("$.reference").value(REFERENCE))
                .andExpect(jsonPath("$.transaction_type").value("PAYMENT"))
                .andRespond(withSuccess("{\"status\":\"PENDING\",\"reference_id\":\"p\"}", MediaType.APPLICATION_JSON));
        assertThat(gateway.collect(request()).state()).isEqualTo(TopupState.PENDING);
        server.verify();
    }

    @Test
    void aRefusedCollectionFailsDefinitively() {
        server.expect(requestTo("http://pvit.test/v2/PAYCODE/rest")).andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));
        PaymentOutcome outcome = gateway.collect(request());
        assertThat(outcome.state()).isEqualTo(TopupState.FAILED);
    }

    @Test
    void anInvalidCustomerNumberFailsWithItsOwnCodeAndKeepsTheKey() {
        server.expect(requestTo("http://pvit.test/v2/PAYCODE/rest")).andRespond(withStatus(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status_code\":3024,\"error\":\"INVALID_CLIENT_ACCOUNT_NUMBER\"}"));
        PaymentOutcome outcome = gateway.collect(request());
        assertThat(outcome.state()).isEqualTo(TopupState.FAILED);
        assertThat(outcome.failureCode()).isEqualTo("INVALID_ACCOUNT_NUMBER");
        server.verify();
    }

    @Test
    void aServerErrorLeavesTheOutcomeUnknown() {
        server.expect(requestTo("http://pvit.test/v2/PAYCODE/rest")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        assertThatThrownBy(() -> gateway.collect(request())).isInstanceOf(PvitException.class);
    }

    @Test
    void aSuccessWithTheRightReferenceAndAmountIsConfirmed() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withSuccess("{\"status\":\"SUCCESS\",\"merchant_reference_id\":\"" + REFERENCE
                        + "\",\"amount\":\"5000.00\",\"reference_id\":\"p-9\"}", MediaType.APPLICATION_JSON));
        PaymentOutcome outcome = gateway.status(REFERENCE);
        assertThat(outcome.state()).isEqualTo(TopupState.SUCCEEDED);
        assertThat(outcome.confirmedMinor()).isEqualTo(5000);
        assertThat(outcome.providerRef()).isEqualTo("p-9");
    }

    @Test
    void anAnswerForAnotherReferenceIsNeverTrusted() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withSuccess("{\"status\":\"SUCCESS\",\"merchant_reference_id\":\"OTHER\","
                        + "\"amount\":\"5000\"}", MediaType.APPLICATION_JSON));
        assertThat(gateway.status(REFERENCE).state()).isEqualTo(TopupState.PENDING);
    }

    @Test
    void aSuccessWithoutAnAmountIsNotCreditable() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withSuccess("{\"status\":\"SUCCESS\",\"merchant_reference_id\":\"" + REFERENCE + "\"}",
                        MediaType.APPLICATION_JSON));
        assertThat(gateway.status(REFERENCE).state()).isEqualTo(TopupState.PENDING);
    }

    @Test
    void aDeclinedPaymentIsReportedAsFailed() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withSuccess("{\"status\":\"FAILED\",\"merchant_reference_id\":\"" + REFERENCE + "\"}",
                        MediaType.APPLICATION_JSON));
        assertThat(gateway.status(REFERENCE).state()).isEqualTo(TopupState.FAILED);
    }

    @Test
    void anOperatorOutsideGabonIsNotSupported() {
        assertThat(gateway.supports("GA", MobileOperator.AIRTEL)).isTrue();
        assertThat(gateway.supports("ML", MobileOperator.AIRTEL)).isFalse();
    }

    @Test
    void anOperatorWithoutItsOwnAccountIsNotSupported() {
        assertThat(gateway.supports("GA", MobileOperator.MOOV)).isFalse();
    }

    @Test
    void aTransactionThePlatformDoesNotKnowYetIsStillPending() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"TRANSACTION_NOT_FOUND\"}"));
        assertThat(gateway.status(REFERENCE).state()).isEqualTo(TopupState.PENDING);
    }

    @Test
    void anyOtherProviderFailureWhileCheckingIsAnUnknownOutcomeNotAFailure() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        assertThatThrownBy(() -> gateway.status(REFERENCE)).isInstanceOf(PvitException.class);
    }

    @Test
    void aRefusalCarriesTheProvidersExplanationForTheLogs() {
        server.expect(requestTo("http://pvit.test/STATUSCODE/status?transactionId=" + REFERENCE
                        + "&accountOperationCode=ACC-1&transactionOperation=PAYMENT"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"CONSTRAINT_VIOLATION\",\"messages\":[\"bad field\"]}"));
        assertThatThrownBy(() -> gateway.status(REFERENCE)).isInstanceOf(PvitException.class)
                .hasMessageContaining("CONSTRAINT_VIOLATION");
    }
}
