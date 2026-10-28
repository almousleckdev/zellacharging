package com.evcharge.customer.wallet.infrastructure.pvit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PvitKycTest {

    private static final String KYC_URL = "http://pvit.test/v2/KYCCODE/kyc"
            + "?customerAccountNumber=077123456&operatorCode=AIRTEL_MONEY";

    private MockRestServiceServer server;
    private PvitKyc kyc;

    private void wire(String kycCode) {
        PvitProperties properties = new PvitProperties("http://pvit.test", "ACC-1", null, "pw", "SECRETCODE",
                "PAYCODE", "STATUSCODE", "", kycCode, "CALLBACK", "ZELLA", "CHARGING", "CUSTOMER",
                Duration.ofSeconds(5));
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.baseUrl());
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        JsonMapper mapper = JsonMapper.builder().build();
        kyc = new PvitKyc(client, new PvitSecretProvider(client, properties, "ACC-1", mapper), properties, mapper);
    }

    @BeforeEach
    void withTheCodeConfigured() {
        wire("KYCCODE");
        server.expect(requestTo("http://pvit.test/v2/SECRETCODE/renew-secret")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"secret\":\"s3\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
    }

    @Test
    void theCustomerIsLookedUpWithTheSecretAndTheOperator() {
        server.expect(requestTo(KYC_URL)).andExpect(header("X-Secret", "s3"))
                .andRespond(withSuccess("{\"data\":{\"status\":\"ACTIVE\"}}", MediaType.APPLICATION_JSON));

        kyc.inspect("AIRTEL_MONEY", "077123456");

        server.verify();
    }

    @Test
    void aRefusedLookupNeverBreaksThePayment() {
        server.expect(requestTo(KYC_URL)).andRespond(withStatus(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"CUSTOMER_UNKNOWN\"}"));

        assertThatCode(() -> kyc.inspect("AIRTEL_MONEY", "077123456")).doesNotThrowAnyException();
        server.verify();
    }

    @Test
    void anUnreachableProviderNeverBreaksThePayment() {
        server.expect(requestTo(KYC_URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatCode(() -> kyc.inspect("AIRTEL_MONEY", "077123456")).doesNotThrowAnyException();
    }

    @Test
    void withoutAConfiguredCodeNothingIsCalled() {
        wire("");

        kyc.inspect("AIRTEL_MONEY", "077123456");

        server.verify();
    }
}
