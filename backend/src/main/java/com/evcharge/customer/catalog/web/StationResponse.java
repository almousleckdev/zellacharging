package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.domain.Station;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record StationResponse(
        String id,
        String isoCode,
        String vendor,
        String model,
        boolean online,
        Instant lastSeenAt,
        BigDecimal latitude,
        BigDecimal longitude,
        Double distanceKm,
        long availableConnectors,
        int totalConnectors,
        List<ConnectorResponse> connectors) {

    static StationResponse from(Station station, Double distanceKm) {
        return new StationResponse(station.id(), station.isoCode(), station.vendor(), station.model(),
                station.online(), station.lastSeenAt(), station.latitude(), station.longitude(),
                distanceKm, station.availableConnectors(), station.connectors().size(),
                station.connectors().stream().map(ConnectorResponse::from).toList());
    }
}
