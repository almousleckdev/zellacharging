package com.evcharge.customer.partner.infrastructure;

import com.evcharge.customer.partner.application.port.SuspensionStore;
import com.evcharge.customer.partner.domain.ActiveSuspension;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcSuspensionStore implements SuspensionStore {

    private final JdbcClient jdbc;

    JdbcSuspensionStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<String> knownNumbers(UUID customerId) {
        return jdbc.sql("select phone_e164 from customer where id = :id and phone_e164 is not null "
                        + "union select msisdn from topup where customer_id = :id and state = 'SUCCEEDED'")
                .param("id", customerId).query(String.class).list();
    }

    @Override
    public List<UUID> customersWithNumber(String partner, String msisdn) {
        return jdbc.sql("select customer_id from suspended_number where partner = :partner and msisdn = :msisdn")
                .param("partner", partner).param("msisdn", msisdn).query(UUID.class).list();
    }

    @Override
    public Optional<ActiveSuspension> active(String partner, UUID customerId) {
        return jdbc.sql("select blocked_before, owed_minor from partner_suspension "
                        + "where partner = :partner and customer_id = :customer and restored_at is null")
                .param("partner", partner).param("customer", customerId)
                .query((rs, row) -> new ActiveSuspension(rs.getBoolean("blocked_before"), rs.getLong("owed_minor")))
                .optional();
    }

    @Override
    public void suspend(String partner, UUID customerId, long owedMinor, String currency, int decimals, boolean blockedBefore, Collection<String> numbers, Instant at) {
        jdbc.sql("insert into partner_suspension (partner, customer_id, owed_minor, currency, decimals, "
                        + "blocked_before, suspended_at) values (:partner, :customer, :owed, :currency, :decimals, "
                        + ":blocked, :at) on conflict (partner, customer_id) do update set "
                        + "owed_minor = excluded.owed_minor, currency = excluded.currency, "
                        + "decimals = excluded.decimals, "
                        + "blocked_before = case when excluded.blocked_before = false then false "
                        + "when partner_suspension.restored_at is null then partner_suspension.blocked_before "
                        + "else excluded.blocked_before end, "
                        + "suspended_at = case when partner_suspension.restored_at is null "
                        + "then partner_suspension.suspended_at else excluded.suspended_at end, restored_at = null")
                .param("partner", partner).param("customer", customerId).param("owed", owedMinor)
                .param("currency", currency).param("decimals", decimals).param("blocked", blockedBefore)
                .param("at", Timestamps.toDb(at)).update();
        for (String number : numbers) {
            jdbc.sql("insert into suspended_number (partner, customer_id, msisdn) "
                            + "values (:partner, :customer, :msisdn) on conflict do nothing")
                    .param("partner", partner).param("customer", customerId).param("msisdn", number).update();
        }
    }

    @Override
    public void lift(String partner, UUID customerId, Instant at) {
        jdbc.sql("delete from suspended_number where partner = :partner and customer_id = :customer")
                .param("partner", partner).param("customer", customerId).update();
        jdbc.sql("update partner_suspension set restored_at = :at where partner = :partner "
                        + "and customer_id = :customer and restored_at is null")
                .param("partner", partner).param("customer", customerId).param("at", Timestamps.toDb(at)).update();
    }
}
