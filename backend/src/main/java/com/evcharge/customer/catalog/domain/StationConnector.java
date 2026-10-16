package com.evcharge.customer.catalog.domain;

import java.math.BigDecimal;

public record StationConnector(int connectorId, String status, String connectorType, BigDecimal maxPowerKw) {

    public boolean isAvailable() {
        return "Available".equals(status);
    }
}
