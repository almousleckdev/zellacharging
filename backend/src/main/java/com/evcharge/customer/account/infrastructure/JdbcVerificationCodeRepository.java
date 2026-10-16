package com.evcharge.customer.account.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.account.application.port.VerificationCodeRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.VerificationCode;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcVerificationCodeRepository implements VerificationCodeRepository {

    private static final RowMapper<VerificationCode> MAPPER = (rs, rowNum) -> new VerificationCode(
            rs.getObject("id", UUID.class),
            rs.getObject("customer_id", UUID.class),
            Channel.valueOf(rs.getString("channel")),
            rs.getString("code_hash"),
            Timestamps.read(rs, "expires_at"),
            rs.getInt("attempts"),
            Timestamps.read(rs, "consumed_at"),
            Timestamps.read(rs, "created_at"));

    private final JdbcClient jdbc;

    JdbcVerificationCodeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(VerificationCode code) {
        jdbc.sql("insert into verification_code (id, customer_id, channel, code_hash, expires_at, attempts, created_at) "
                        + "values (:id, :customerId, :channel, :hash, :expiresAt, 0, :createdAt)")
                .param("id", code.id()).param("customerId", code.customerId())
                .param("channel", code.channel().name()).param("hash", code.codeHash())
                .param("expiresAt", Timestamps.toDb(code.expiresAt()))
                .param("createdAt", Timestamps.toDb(code.createdAt())).update();
    }

    @Override
    public Optional<VerificationCode> findLatest(UUID customerId, Channel channel) {
        return jdbc.sql("select * from verification_code where customer_id = :customerId and channel = :channel "
                        + "order by created_at desc limit 1")
                .param("customerId", customerId).param("channel", channel.name()).query(MAPPER).optional();
    }

    @Override
    public void incrementAttempts(UUID id) {
        jdbc.sql("update verification_code set attempts = attempts + 1 where id = :id").param("id", id).update();
    }

    @Override
    public void consume(UUID id, Instant at) {
        jdbc.sql("update verification_code set consumed_at = :at where id = :id")
                .param("at", Timestamps.toDb(at)).param("id", id).update();
    }
}
