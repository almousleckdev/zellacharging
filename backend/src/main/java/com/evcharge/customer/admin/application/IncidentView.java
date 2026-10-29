package com.evcharge.customer.admin.application;

import com.evcharge.customer.shared.Incident;

public record IncidentView(String country, Incident incident, String status, String note) {
}
