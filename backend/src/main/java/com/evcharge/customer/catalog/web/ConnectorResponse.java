package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.domain.StationConnector;

import java.math.BigDecimal;

record ConnectorResponse(
        int connectorId, String status, boolean available, String connectorType, BigDecimal maxPowerKw) {

    static ConnectorResponse from(StationConnector connector) {
        return new ConnectorResponse(connector.connectorId(), connector.status(), connector.isAvailable(),
                connector.connectorType(), connector.maxPowerKw());
    }
}
