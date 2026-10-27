package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.domain.RemoteSession;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppSessionPage(List<OcppSessionPayload> items, Long total) {

    List<RemoteSession> sessions() {
        return items == null ? List.of() : items.stream().map(OcppSessionPayload::toDomain).toList();
    }
}
