package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.account.application.port.CustomerIdentityRepository;
import com.evcharge.customer.account.domain.SocialProvider;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcCustomerIdentityRepository implements CustomerIdentityRepository {

    private final JdbcClient jdbc;

    JdbcCustomerIdentityRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<UUID> customerOf(SocialProvider provider, String subject) {
        return jdbc.sql("select customer_id from customer_identity where provider = :provider and subject = :subject")
                .param("provider", provider.name()).param("subject", subject).query(UUID.class).optional();
    }

    @Override
    public boolean isLinked(UUID customerId, SocialProvider provider, String subject) {
        return jdbc.sql("""
                        select count(*) from customer_identity
                        where customer_id = :customer and provider = :provider and subject = :subject
                        """)
                .param("customer", customerId).param("provider", provider.name()).param("subject", subject)
                .query(Integer.class).single() > 0;
    }

    @Override
    public void link(UUID customerId, SocialProvider provider, String subject, Instant at) {
        jdbc.sql("""
                        insert into customer_identity (id, customer_id, provider, subject, created_at)
                        values (:id, :customer, :provider, :subject, :at)
                        """)
                .param("id", UUID.randomUUID()).param("customer", customerId).param("provider", provider.name())
                .param("subject", subject).param("at", Timestamps.toDb(at)).update();
    }

    @Override
    public void deleteOf(UUID customerId) {
        jdbc.sql("delete from customer_identity where customer_id = :customer").param("customer", customerId)
                .update();
    }
}
