package com.evcharge.customer.admin.application.port;

import com.evcharge.customer.admin.application.IncidentReview;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface IncidentReviews {

    Map<String, IncidentReview> ofCountry(String country);

    void save(String country, String key, String status, String note, UUID adminId, Instant at);
}
