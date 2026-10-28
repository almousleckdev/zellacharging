package com.evcharge.customer.wallet.infrastructure;

import com.evcharge.customer.wallet.CustomerWallet;
import com.evcharge.customer.wallet.WalletFigures;
import com.evcharge.customer.wallet.WalletStatistics;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcWalletStatistics implements WalletStatistics {

    private final JdbcClient jdbc;

    JdbcWalletStatistics(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public WalletFigures figures() {
        long[] wallets = jdbc.sql("select count(*), coalesce(sum(balance_minor), 0)::bigint, "
                        + "coalesce(sum(reserved_minor), 0)::bigint from wallet "
                        + "where country_code = current_setting('app.country', true)")
                .query((rs, rowNum) -> new long[]{rs.getLong(1), rs.getLong(2), rs.getLong(3)}).single();
        long[] topups = jdbc.sql("select coalesce(sum(amount_minor) filter (where state = 'SUCCEEDED'), 0)::bigint, "
                        + "count(*) filter (where state = 'PENDING') from topup "
                        + "where country_code = current_setting('app.country', true)")
                .query((rs, rowNum) -> new long[]{rs.getLong(1), rs.getLong(2)}).single();
        long[] withdrawals = jdbc.sql("select coalesce(sum(amount_minor) filter (where state = 'PAID'), 0)::bigint, "
                        + "count(*) filter (where state in ('REQUESTED', 'PROCESSING', 'REVIEW')) from withdrawal "
                        + "where country_code = current_setting('app.country', true)")
                .query((rs, rowNum) -> new long[]{rs.getLong(1), rs.getLong(2)}).single();
        long captured = jdbc.sql("select coalesce(sum(amount_minor), 0)::bigint from ledger_entry "
                        + "where type = 'CAPTURE' and country_code = current_setting('app.country', true)")
                .query(Long.class).single();
        return new WalletFigures(wallets[0], wallets[1], wallets[2], topups[0], topups[1], withdrawals[0],
                withdrawals[1], captured);
    }

    @Override
    public Optional<CustomerWallet> ofCustomer(UUID customerId) {
        return jdbc.sql("select w.currency, w.decimals, w.balance_minor, w.reserved_minor, "
                        + "(select coalesce(sum(amount_minor), 0)::bigint from topup t where t.customer_id = w.customer_id "
                        + "and t.state = 'SUCCEEDED') as topups, "
                        + "(select count(*) from topup t where t.customer_id = w.customer_id and t.state = 'PENDING') "
                        + "as pending, "
                        + "(select coalesce(sum(amount_minor), 0)::bigint from withdrawal x "
                        + "where x.customer_id = w.customer_id and x.state = 'PAID') as withdrawn "
                        + "from wallet w where w.customer_id = :id "
                        + "and w.country_code = current_setting('app.country', true)")
                .param("id", customerId)
                .query((rs, rowNum) -> new CustomerWallet(rs.getString("currency").trim(), rs.getInt("decimals"),
                        rs.getLong("balance_minor"), rs.getLong("reserved_minor"), rs.getLong("topups"),
                        rs.getLong("pending"), rs.getLong("withdrawn"))).optional();
    }
}
