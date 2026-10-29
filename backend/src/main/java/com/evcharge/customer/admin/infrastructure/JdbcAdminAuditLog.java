package com.evcharge.customer.admin.infrastructure;

import com.evcharge.customer.admin.application.AuditEntry;
import com.evcharge.customer.admin.application.AuditPage;
import com.evcharge.customer.admin.application.AuditQuery;
import com.evcharge.customer.admin.application.port.AdminAuditLog;
import com.evcharge.customer.shared.LikePattern;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class JdbcAdminAuditLog implements AdminAuditLog {

    private static final String FROM = " from admin_audit_record a join admin_user u on u.id = a.admin_id where true";
    private static final String TEXT = " and (lower(u.email) like :pattern escape '\\' "
            + "or lower(coalesce(a.subject, '')) like :pattern escape '\\' "
            + "or lower(coalesce(a.detail, '')) like :pattern escape '\\')";

    private static final RowMapper<AuditEntry> MAPPER = (rs, rowNum) -> new AuditEntry(
            rs.getObject("id", UUID.class), rs.getObject("admin_id", UUID.class), rs.getString("email"),
            rs.getString("country_code"), rs.getString("action"), rs.getString("subject"), rs.getString("detail"),
            rs.getString("correlation_id"), Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcAdminAuditLog(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AuditPage search(AuditQuery query) {
        String where = (query.includeGlobal() ? " and (a.country_code is null or a.country_code in (:countries))"
                : " and a.country_code in (:countries)")
                + (query.action() == null ? "" : " and a.action = :action")
                + (query.text() == null ? "" : TEXT) + (query.subject() == null ? "" : " and a.subject = :subject") + query.filter().clause("a.created_at", "a.admin_id");
        long total = bind(jdbc.sql("select count(*)" + FROM + where), query).query(Long.class).single();
        var items = bind(jdbc.sql("select a.*, u.email" + FROM + where
                + " order by a.created_at desc, a.id limit :limit offset :offset"), query)
                .param("limit", query.limit()).param("offset", query.offset()).query(MAPPER).list();
        return new AuditPage(total, items);
    }

    @Override
    public List<String> actions() {
        return jdbc.sql("select distinct action from admin_audit_record order by action").query(String.class).list();
    }

    private static JdbcClient.StatementSpec bind(JdbcClient.StatementSpec spec, AuditQuery query) {
        spec = query.filter().bind(spec).param("countries", query.countries());
        if (query.action() != null) {
            spec = spec.param("action", query.action());
        }
        if (query.subject() != null) {
            spec = spec.param("subject", query.subject());
        }
        return query.text() == null ? spec : spec.param("pattern", LikePattern.contains(query.text()));
    }
}
