package com.evcharge.customer.charging.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record OcppSessionPage(List<OcppSessionPayload> items) {
}
