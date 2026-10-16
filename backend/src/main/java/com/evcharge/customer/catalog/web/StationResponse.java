package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.domain.Station;

import java.math.BigDecimal;
import java.util.List;

record StationResponse(
        String id,
        boolean online,
        BigDecimal latitude,
        BigDecimal longitude,
        Double distanceKm,
        long availableConnectors,
        int totalConnectors,
        List<ConnectorResponse> connectors) {

    static StationResponse from(Station station, Double distanceKm) {
        return new StationResponse(station.id(), station.online(), station.latitude(), station.longitude(),
                distanceKm, station.availableConnectors(), station.connectors().size(),
                station.connectors().stream().map(ConnectorResponse::from).toList());
    }
}
