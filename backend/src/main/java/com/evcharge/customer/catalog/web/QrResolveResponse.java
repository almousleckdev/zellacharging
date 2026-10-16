package com.evcharge.customer.catalog.web;

import com.evcharge.customer.catalog.application.ResolvedConnector;

record QrResolveResponse(StationResponse station, ConnectorResponse connector) {

    static QrResolveResponse from(ResolvedConnector resolved) {
        return new QrResolveResponse(StationResponse.from(resolved.station(), null),
                ConnectorResponse.from(resolved.connector()));
    }
}
