package com.evcharge.customer.catalog.infrastructure;

import com.evcharge.customer.catalog.InventoryConnector;
import com.evcharge.customer.catalog.StationInventory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppInventoryPayload(String id, String isoCode, boolean online, String vendor, String model,
                            Instant lastSeenAt, BigDecimal latitude, BigDecimal longitude,
                            List<OcppConnectorPayload> connectors) {

    StationInventory toInventory() {
        List<InventoryConnector> mapped = connectors == null ? List.of() : connectors.stream()
                .map(c -> new InventoryConnector(c.connectorId(), c.status(), c.connectorType(), c.maxPowerKw()))
                .toList();
        return new StationInventory(id, isoCode, online, vendor, model, lastSeenAt, latitude, longitude, mapped);
    }
}
