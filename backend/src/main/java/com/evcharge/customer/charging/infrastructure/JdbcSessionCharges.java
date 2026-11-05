package com.evcharge.customer.charging.infrastructure;

import com.evcharge.customer.charging.application.port.SessionCharges;
import com.evcharge.customer.charging.domain.SessionCharge;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcSessionCharges implements SessionCharges {

    private static final UUID NO_SESSION = new UUID(0, 0);

    private final JdbcClient jdbc;

    JdbcSessionCharges(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insertIfAbsent(SessionCharge charge) {
        jdbc.sql("insert into session_charge (session_id, customer_id, state, energy_cost_minor, vat_minor, "
                        + "service_fee_minor, total_minor, currency, decimals, estimated, updated_at) values "
                        + "(:session, :customer, :state, :energy, :vat, :service, :total, :currency, :decimals, "
                        + ":estimated, :at) on conflict (session_id) do nothing")
                .param("session", charge.sessionId()).param("customer", charge.customerId())
                .param("state", charge.state().name()).param("energy", charge.energyCostMinor())
                .param("vat", charge.vatMinor()).param("service", charge.serviceFeeMinor())
                .param("total", charge.totalMinor()).param("currency", charge.currency())
                .param("decimals", charge.decimals()).param("estimated", charge.estimated())
                .param("at", Timestamps.toDb(charge.updatedAt())).update();
    }

    @Override
    public List<UUID> terminalSessionsWithoutCharge(int limit, Collection<UUID> excluded) {
        return jdbc.sql("select s.id from charging_session s where s.state in ('COMPLETED', 'ABANDONED', 'FAILED') "
                        + "and s.country_code = current_setting('app.country') and s.id not in (:excluded) "
                        + "and not exists (select 1 from session_charge c where c.session_id = s.id) "
                        + "order by s.created_at limit :limit")
                .param("limit", limit).param("excluded", excluded.isEmpty() ? List.of(NO_SESSION) : excluded)
                .query(UUID.class).list();
    }
}
