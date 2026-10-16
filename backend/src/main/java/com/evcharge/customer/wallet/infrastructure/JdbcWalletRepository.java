package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.shared.Timestamps;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.WalletBalance;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
class JdbcWalletRepository implements WalletRepository {

    private static final RowMapper<WalletBalance> MAPPER = (rs, rowNum) -> new WalletBalance(
            rs.getLong("balance_minor"), rs.getLong("reserved_minor"), rs.getString("currency"),
            rs.getInt("decimals"));

    private final JdbcClient jdbc;

    JdbcWalletRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ensure(UUID customerId, String currency, int decimals) {
        jdbc.sql("insert into wallet (customer_id, currency, decimals, created_at) "
                        + "values (:id, :currency, :decimals, :now) on conflict do nothing")
                .param("id", customerId).param("currency", currency).param("decimals", decimals)
                .param("now", Timestamps.toDb(Instant.now())).update();
    }

    @Override
    public WalletBalance lock(UUID customerId) {
        return jdbc.sql("select * from wallet where customer_id = :id for update")
                .param("id", customerId).query(MAPPER).single();
    }

    @Override
    public WalletBalance read(UUID customerId) {
        return jdbc.sql("select * from wallet where customer_id = :id")
                .param("id", customerId).query(MAPPER).single();
    }

    @Override
    public void apply(UUID customerId, long balanceDelta, long reservedDelta) {
        jdbc.sql("update wallet set balance_minor = balance_minor + :balance, "
                        + "reserved_minor = reserved_minor + :reserved where customer_id = :id")
                .param("balance", balanceDelta).param("reserved", reservedDelta).param("id", customerId).update();
    }
}
