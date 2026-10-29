package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.StationInventories;
import com.evcharge.customer.catalog.StationInventory;
import com.evcharge.customer.shared.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
class OcppStationInventories implements StationInventories {

    private static final Logger log = LoggerFactory.getLogger(OcppStationInventories.class);

    private final RestClient client;

    OcppStationInventories(RestClient ocppRestClient) {
        this.client = ocppRestClient;
    }

    @Override
    public List<StationInventory> all() {
        try {
            List<OcppInventoryPayload> payload = client.get().uri("/api/v1/charge-points").retrieve()
                    .body(new ParameterizedTypeReference<List<OcppInventoryPayload>>() {
                    });
            return payload == null ? List.of() : payload.stream().map(OcppInventoryPayload::toInventory).toList();
        } catch (RestClientException failure) {
            throw unavailable(failure);
        }
    }

    @Override
    public Optional<StationInventory> find(String stationId) {
        try {
            return Optional.ofNullable(client.get().uri("/api/v1/charge-points/{id}", stationId).retrieve()
                    .body(OcppInventoryPayload.class)).map(OcppInventoryPayload::toInventory);
        } catch (HttpClientErrorException.NotFound missing) {
            return Optional.empty();
        } catch (RestClientException failure) {
            throw unavailable(failure);
        }
    }

    private static ApiException unavailable(RestClientException failure) {
        log.warn("Charging platform call failed: {}", failure.getMessage());
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "CATALOG_UNAVAILABLE",
                "Charging network is temporarily unavailable");
    }
}
