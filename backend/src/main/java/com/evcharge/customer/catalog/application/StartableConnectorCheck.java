package com.evcharge.customer.catalog.application;

import com.evcharge.customer.catalog.application.port.StationDirectory;
import com.evcharge.customer.catalog.domain.Station;
import com.evcharge.customer.catalog.domain.StationConnector;
import com.evcharge.customer.charging.ConnectorOccupancy;
import com.evcharge.customer.charging.StartableConnectors;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class StartableConnectorCheck implements StartableConnectors {

    private final StationDirectory directory;
    private final ConnectorOccupancy occupancy;

    StartableConnectorCheck(StationDirectory directory, ConnectorOccupancy occupancy) {
        this.directory = directory;
        this.occupancy = occupancy;
    }

    @Override
    public void require(String stationId, int connectorId) {
        Station station = directory.find(stationId)
                .filter(found -> TenantContext.current().filter(c -> c.equalsIgnoreCase(found.isoCode())).isPresent())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Station not found"));
        if (!station.online()) {
            throw new ApiException(HttpStatus.CONFLICT, "STATION_OFFLINE", "The station is offline");
        }
        StationConnector connector = station.connector(connectorId).orElseThrow(
                () -> new ApiException(HttpStatus.NOT_FOUND, "CONNECTOR_NOT_FOUND", "Connector not found"));
        if (!connector.isStartable()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONNECTOR_UNAVAILABLE", "The connector is not available");
        }
        if (occupancy.busyConnectors().getOrDefault(stationId, Set.of()).contains(connectorId)) {
            throw new ApiException(HttpStatus.CONFLICT, "CONNECTOR_OR_SESSION_BUSY", "The connector is in use");
        }
    }
}
