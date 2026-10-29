package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.IncidentReview;
import com.evcharge.customer.admin.application.port.IncidentReviews;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
class JdbcIncidentReviews implements IncidentReviews {

    private final JdbcClient jdbc;

    JdbcIncidentReviews(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<String, IncidentReview> ofCountry(String country) {
        Map<String, IncidentReview> reviews = new HashMap<>();
        jdbc.sql("select incident_key, status, note from incident_review where country_code = :country")
                .param("country", country).query((rs, rowNum) -> reviews.put(rs.getString(1),
                        new IncidentReview(rs.getString(2), rs.getString(3)))).list();
        return reviews;
    }

    @Override
    public void save(String country, String key, String status, String note, UUID adminId, Instant at) {
        jdbc.sql("insert into incident_review (country_code, incident_key, status, note, admin_id, updated_at) "
                        + "values (:country, :key, :status, :note, :admin, :at) on conflict (country_code, incident_key) "
                        + "do update set status = :status, note = :note, admin_id = :admin, updated_at = :at")
                .param("country", country).param("key", key).param("status", status).param("note", note)
                .param("admin", adminId).param("at", Timestamps.toDb(at)).update();
    }
}
