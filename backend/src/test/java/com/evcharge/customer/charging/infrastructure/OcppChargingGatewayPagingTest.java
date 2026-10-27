package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.shared.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class OcppChargingGatewayPagingTest {

    private static final String ACTIVE = "http://ocpp.test/api/v1/sessions?status=ACTIVE&size=100&page=";

    private MockRestServiceServer server;
    private OcppChargingGateway gateway;

    @BeforeEach
    void bind() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ocpp.test");
        server = MockRestServiceServer.bindTo(builder).build();
        gateway = new OcppChargingGateway(builder.build());
    }

    private static String page(int index, int from, int to, int total) {
        String items = IntStream.range(from, to).mapToObj(id -> "{\"transactionId\":" + id
                + ",\"deviceId\":\"CP-1\",\"connectorId\":1,\"status\":\"ACTIVE\",\"idTag\":\"Z-TAG\"}")
                .collect(Collectors.joining(","));
        return "{\"items\":[" + items + "],\"page\":" + index + ",\"size\":100,\"total\":" + total + "}";
    }

    private void expectPage(int index, String body) {
        server.expect(requestTo(ACTIVE + index)).andExpect(method(GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    @Test
    void activeSessionsAreReadAcrossEveryPage() {
        expectPage(0, page(0, 0, 100, 130));
        expectPage(1, page(1, 100, 130, 130));

        List<RemoteSession> sessions = gateway.activeSessions();

        assertThat(sessions).hasSize(130);
        server.verify();
    }

    @Test
    void aSmallFleetIsASingleRequest() {
        expectPage(0, page(0, 0, 3, 3));

        assertThat(gateway.activeSessions()).hasSize(3);
        server.verify();
    }

    @Test
    void aFailingLaterPageFailsTheWholeReadInsteadOfReturningAPartialFleet() {
        expectPage(0, page(0, 0, 100, 130));
        server.expect(requestTo(ACTIVE + 1)).andRespond(withServerError());

        assertThatThrownBy(() -> gateway.activeSessions()).isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).code()).isEqualTo("CHARGING_UNAVAILABLE");
    }
}
