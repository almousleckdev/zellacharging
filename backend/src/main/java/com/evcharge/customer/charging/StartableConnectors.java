package com.evcharge.customer.charging;

public interface StartableConnectors {

    void require(String stationId, int connectorId);
}
