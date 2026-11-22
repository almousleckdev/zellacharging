package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.FreeSundayClaims;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcFreeSundayClaims implements FreeSundayClaims {

    private static final String SCOPE = "country_code = current_setting('app.country', true)";

    private final JdbcClient jdbc;

    JdbcFreeSundayClaims(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean claim(UUID customerId, LocalDate sundayDate, UUID sessionId, UUID agreementId, Instant windowEndsAt) {
        return jdbc.sql("insert into free_sunday_claim (id, customer_id, sunday_date, session_id, agreement_id, "
                        + "window_ends_at) values (:id, :customer, :sunday, :session, :agreement, :windowEnd) "
                        + "on conflict do nothing")
                .param("id", UUID.randomUUID()).param("customer", customerId).param("sunday", sundayDate)
                .param("session", sessionId).param("agreement", agreementId)
                .param("windowEnd", Timestamps.toDb(windowEndsAt)).update() == 1;
    }

    @Override
    public boolean isClaimed(UUID agreementId, UUID customerId, LocalDate sundayDate) {
        return jdbc.sql("select exists (select 1 from free_sunday_claim where (customer_id = :customer "
                        + "or agreement_id = :agreement) and sunday_date = :sunday and state = 'CLAIMED' and "
                        + SCOPE + ")")
                .param("customer", customerId).param("agreement", agreementId).param("sunday", sundayDate)
                .query(Boolean.class).single();
    }

    @Override
    public void release(UUID sessionId) {
        jdbc.sql("update free_sunday_claim set state = 'RELEASED', released_at = now() "
                        + "where session_id = :session and state = 'CLAIMED' and " + SCOPE)
                .param("session", sessionId).update();
    }

    @Override
    public Optional<UUID> agreementOf(UUID sessionId) {
        return jdbc.sql("select agreement_id from free_sunday_claim "
                        + "where session_id = :session and state = 'CLAIMED' and " + SCOPE)
                .param("session", sessionId).query(UUID.class).optional();
    }

    @Override
    public List<UUID> sessionsPastWindow(Instant since, Instant now) {
        return jdbc.sql("select session_id from free_sunday_claim where state = 'CLAIMED' "
                        + "and window_ends_at > :since and window_ends_at <= :now and " + SCOPE
                        + " order by window_ends_at")
                .param("since", Timestamps.toDb(since)).param("now", Timestamps.toDb(now))
                .query(UUID.class).list();
    }

    @Override
    public List<UUID> sessionsEndingBetween(Instant from, Instant to) {
        return jdbc.sql("select session_id from free_sunday_claim where state = 'CLAIMED' "
                        + "and window_ends_at > :from and window_ends_at <= :to and " + SCOPE
                        + " order by window_ends_at")
                .param("from", Timestamps.toDb(from)).param("to", Timestamps.toDb(to))
                .query(UUID.class).list();
    }

    @Override
    public Optional<Instant> windowEnd(UUID sessionId) {
        return jdbc.sql("select window_ends_at from free_sunday_claim where session_id = :session "
                        + "and state = 'CLAIMED' and " + SCOPE)
                .param("session", sessionId).query((rs, row) -> Timestamps.read(rs, "window_ends_at")).optional();
    }

    @Override
    public List<UUID> releaseOrphans(Instant claimedSince, Instant claimedBefore) {
        return jdbc.sql("update free_sunday_claim c set state = 'RELEASED', released_at = now() "
                        + "where c.state = 'CLAIMED' and c.claimed_at >= :since and c.claimed_at < :before and c."
                        + SCOPE + " and not exists (select 1 from charging_session s where s.id = c.session_id) "
                        + "and not exists (select 1 from funding_hold h where h.session_id = c.session_id "
                        + "and h.state = 'OPEN') returning c.session_id")
                .param("since", Timestamps.toDb(claimedSince)).param("before", Timestamps.toDb(claimedBefore))
                .query(UUID.class).list();
    }
}
