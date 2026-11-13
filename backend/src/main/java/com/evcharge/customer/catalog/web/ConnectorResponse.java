package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.domain.StationConnector;

import java.math.BigDecimal;

record ConnectorResponse(int connectorId, String status, boolean available, boolean startable,
                         String connectorType, BigDecimal maxPowerKw, Integer freeInMinutes) {

    static ConnectorResponse from(StationConnector connector) {
        return new ConnectorResponse(connector.connectorId(), connector.status(), connector.isAvailable(),
                connector.isStartable(), connector.connectorType(), connector.maxPowerKw(),
                connector.freeInMinutes());
    }
}
