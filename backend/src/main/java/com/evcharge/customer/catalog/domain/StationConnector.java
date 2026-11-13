package com.evcharge.customer.catalog.domain;

import java.math.BigDecimal;

public record StationConnector(int connectorId, String status, String connectorType, BigDecimal maxPowerKw,
                               Integer freeInMinutes) {

    public StationConnector(int connectorId, String status, String connectorType, BigDecimal maxPowerKw) {
        this(connectorId, status, connectorType, maxPowerKw, null);
    }

    public boolean isAvailable() {
        return "Available".equals(status);
    }

    public boolean isStartable() {
        return isAvailable() || "Preparing".equals(status);
    }

    public StationConnector inUse(Integer minutes) {
        String occupied = isAvailable() || "Preparing".equals(status) ? "Charging" : status;
        boolean charging = "Charging".equals(occupied) || "Finishing".equals(occupied);
        return new StationConnector(connectorId, occupied, connectorType, maxPowerKw, charging ? minutes : null);
    }
}
