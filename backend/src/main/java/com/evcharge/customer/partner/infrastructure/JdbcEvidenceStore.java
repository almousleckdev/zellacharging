package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.EvidenceStore;
import com.evcharge.customer.partner.domain.EvidenceEvent;
import com.evcharge.customer.partner.domain.EvidenceItem;
import com.evcharge.customer.partner.domain.EvidenceReference;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcEvidenceStore implements EvidenceStore {

    private final JdbcClient jdbc;

    JdbcEvidenceStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean record(EvidenceEvent event) {
        return jdbc.sql("insert into suspension_evidence (id, partner, suspended_customer_id, "
                        + "receiving_customer_id, receiving_ref, receiving_linked, kind, msisdn, amount_minor, "
                        + "currency, idempotency_key, occurred_at) values (:id, :partner, :suspended, :receiving, "
                        + ":ref, :linked, :kind, :msisdn, :amount, :currency, :key, :at) on conflict do nothing")
                .param("id", UUID.randomUUID()).param("partner", event.partner())
                .param("suspended", event.suspendedCustomerId()).param("receiving", event.receivingCustomerId())
                .param("ref", EvidenceReference.of(event.receivingCustomerId()))
                .param("linked", event.receivingLinked()).param("kind", event.kind())
                .param("msisdn", event.msisdn()).param("amount", event.amountMinor())
                .param("currency", event.currency()).param("key", event.idempotencyKey())
                .param("at", Timestamps.toDb(event.occurredAt())).update() == 1;
    }

    @Override
    public List<EvidenceItem> after(String partner, long sequence, int size, UUID suspendedCustomerId, Instant visibleBefore) {
        return jdbc.sql("select e.seq, e.id, e.suspended_customer_id, e.kind, e.receiving_ref, e.receiving_linked, "
                        + "case when e.receiving_linked then e.receiving_customer_id end as receiving_customer_id, "
                        + "e.amount_minor, e.currency, e.occurred_at from suspension_evidence e "
                        + "where e.partner = :partner and e.seq > :sequence and e.occurred_at <= :until "
                        + "and (cast(:customer as uuid) is null or e.suspended_customer_id = cast(:customer as uuid)) "
                        + "and exists (select 1 from partner_link l where l.partner = e.partner "
                        + "and l.customer_id = e.suspended_customer_id) order by e.seq limit :size")
                .param("partner", partner).param("sequence", sequence).param("customer", suspendedCustomerId)
                .param("size", size).param("until", Timestamps.toDb(visibleBefore))
                .query((rs, row) -> new EvidenceItem(rs.getLong("seq"), rs.getObject("id", UUID.class),
                        rs.getObject("suspended_customer_id", UUID.class), rs.getString("kind"),
                        rs.getString("receiving_ref"), rs.getBoolean("receiving_linked"),
                        rs.getObject("receiving_customer_id", UUID.class), rs.getLong("amount_minor"),
                        rs.getString("currency"), Timestamps.read(rs, "occurred_at")))
                .list();
    }
}
