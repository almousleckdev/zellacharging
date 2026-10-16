package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.ChargingGateway;
import com.evcharge.customer.charging.domain.CommandOutcome;
import com.evcharge.customer.charging.domain.Metering;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.shared.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class OcppChargingGateway implements ChargingGateway {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final RestClient client;

    OcppChargingGateway(RestClient ocppRestClient) {
        this.client = ocppRestClient;
    }

    @Override
    public boolean registerIdTag(String idTag) {
        return call(() -> client.post().uri("/api/v1/id-tags").body(Map.of("idTag", idTag))
                .exchange((request, response) -> response.getStatusCode().is2xxSuccessful()));
    }

    @Override
    public CommandOutcome start(String stationId, int connectorId, String idTag, String idempotencyKey) {
        return command(client.post()
                .uri("/api/v1/charge-points/{id}/connectors/{c}/start", stationId, connectorId)
                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                .body(Map.of("idTag", idTag)));
    }

    @Override
    public CommandOutcome stop(String stationId, int connectorId, String idempotencyKey) {
        return command(client.post()
                .uri("/api/v1/charge-points/{id}/connectors/{c}/stop", stationId, connectorId)
                .header(IDEMPOTENCY_HEADER, idempotencyKey)
                .body(Map.of()));
    }

    @Override
    public List<RemoteSession> sessionsOf(String idTag) {
        OcppSessionPage page = call(() -> client.get()
                .uri(uri -> uri.path("/api/v1/sessions").queryParam("idTag", idTag)
                        .queryParam("size", 20).build())
                .retrieve().body(OcppSessionPage.class));
        return page == null || page.items() == null ? List.of()
                : page.items().stream().map(OcppSessionPayload::toDomain).toList();
    }

    @Override
    public Optional<Metering> metering(int transactionId) {
        try {
            OcppMeteringPayload payload = call(() -> client.get()
                    .uri("/api/v1/sessions/{id}/metering", transactionId)
                    .retrieve().body(OcppMeteringPayload.class));
            return Optional.ofNullable(payload).map(OcppMeteringPayload::toDomain);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private CommandOutcome command(RestClient.RequestBodySpec request) {
        return call(() -> request.exchange((req, response) -> {
            Map<String, Object> body = response.bodyTo(new ParameterizedTypeReference<Map<String, Object>>() {
            });
            if (response.getStatusCode().is2xxSuccessful()) {
                return CommandOutcome.success();
            }
            Object code = body == null ? null : body.get("reasonCode");
            Object message = body == null ? null : body.get("failureReason");
            return CommandOutcome.refused(code == null ? statusCode(response.getStatusCode().value())
                    : code.toString(), message == null ? null : message.toString());
        }));
    }

    private static String statusCode(int status) {
        return status == 504 ? "CHARGER_TIMEOUT" : status == 409 ? "CHARGER_REFUSED" : "CHARGER_UNREACHABLE";
    }

    private <T> T call(java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw e;
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CHARGING_UNAVAILABLE",
                    "Charging network is temporarily unavailable");
        }
    }
}
