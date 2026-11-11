package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.shared.SuspensionEvidenceFeed;
import com.evcharge.customer.shared.SuspensionEvidenceRow;
import com.evcharge.customer.shared.SuspensionEvidenceRow.Person;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcSuspensionEvidenceFeed implements SuspensionEvidenceFeed {

    private final JdbcClient jdbc;

    JdbcSuspensionEvidenceFeed(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<SuspensionEvidenceRow> recent(int limit) {
        return jdbc.sql("select e.id, e.kind, e.occurred_at, e.amount_minor, e.currency, e.receiving_linked, "
                        + "s.id as s_id, s.first_name || ' ' || s.last_name as s_name, s.phone_e164 as s_phone, "
                        + "s.email as s_email, r.id as r_id, r.first_name || ' ' || r.last_name as r_name, "
                        + "r.phone_e164 as r_phone, r.email as r_email from suspension_evidence e "
                        + "join customer s on s.id = e.suspended_customer_id "
                        + "left join customer r on r.id = e.receiving_customer_id "
                        + "order by e.occurred_at desc limit :limit")
                .param("limit", limit).query(JdbcSuspensionEvidenceFeed::row).list();
    }

    private static SuspensionEvidenceRow row(ResultSet rs, int index) throws SQLException {
        return new SuspensionEvidenceRow(rs.getObject("id", UUID.class), rs.getString("kind"),
                Timestamps.read(rs, "occurred_at"), rs.getLong("amount_minor"), rs.getString("currency"),
                rs.getBoolean("receiving_linked"), person(rs, "s"), person(rs, "r"));
    }

    private static Person person(ResultSet rs, String prefix) throws SQLException {
        return new Person(rs.getObject(prefix + "_id", UUID.class), rs.getString(prefix + "_name"),
                rs.getString(prefix + "_phone"), rs.getString(prefix + "_email"));
    }
}
