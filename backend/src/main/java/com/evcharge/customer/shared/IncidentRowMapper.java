package com.evcharge.customer.shared;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class IncidentRowMapper implements RowMapper<Incident> {

    public static final IncidentRowMapper INSTANCE = new IncidentRowMapper();

    private IncidentRowMapper() {
    }

    @Override
    public Incident mapRow(ResultSet rs, int rowNum) throws SQLException {
        String currency = rs.getString(9);
        return new Incident(rs.getString(1), rs.getString(2), rs.getString(3), rs.getObject(4, UUID.class),
                rs.getObject(5, UUID.class), rs.getString(6), rs.getString(7), rs.getObject(8, Long.class),
                currency == null ? null : currency.trim(), rs.getInt(10), Timestamps.read(rs, 11));
    }
}
