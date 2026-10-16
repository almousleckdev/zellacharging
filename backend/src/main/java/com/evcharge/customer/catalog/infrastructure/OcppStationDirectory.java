package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.application.port.StationDirectory;
import com.evcharge.customer.catalog.domain.ConnectorRef;
import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.shared.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
class OcppStationDirectory implements StationDirectory {

    private record QrMapping(String deviceId, int connectorId) {
    }

    private final RestClient client;

    OcppStationDirectory(RestClient ocppRestClient) {
        this.client = ocppRestClient;
    }

    @Override
    public List<Station> list(Double latitude, Double longitude, Double radiusKm) {
        List<OcppStationPayload> payload = call(() -> client.get()
                .uri(uri -> {
                    uri.path("/api/v1/charge-points");
                    if (latitude != null) {
                        uri.queryParam("lat", latitude).queryParam("lng", longitude);
                        uri.queryParam("radiusKm", radiusKm == null ? 25.0 : radiusKm);
                    }
                    return uri.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<OcppStationPayload>>() {
                }));
        return payload == null ? List.of() : payload.stream().map(OcppStationPayload::toDomain).toList();
    }

    @Override
    public Optional<Station> find(String stationId) {
        return notFoundAsEmpty(() -> client.get()
                .uri("/api/v1/charge-points/{id}", stationId)
                .retrieve()
                .body(OcppStationPayload.class)).map(OcppStationPayload::toDomain);
    }

    @Override
    public Optional<ConnectorRef> resolveQr(String token) {
        return notFoundAsEmpty(() -> client.get()
                .uri("/c/{token}", token)
                .retrieve()
                .body(QrMapping.class)).map(mapping -> new ConnectorRef(mapping.deviceId(), mapping.connectorId()));
    }

    private <T> Optional<T> notFoundAsEmpty(Supplier<T> request) {
        try {
            return Optional.ofNullable(call(request));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.NotFound e) {
            throw e;
        } catch (RestClientException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CATALOG_UNAVAILABLE",
                    "Charging network is temporarily unavailable");
        }
    }
}
