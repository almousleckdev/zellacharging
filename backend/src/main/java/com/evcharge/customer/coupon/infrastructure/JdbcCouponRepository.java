package com.evcharge.customer.coupon.infrastructure;

import com.evcharge.customer.coupon.application.port.CouponRepository;
import com.evcharge.customer.coupon.domain.Coupon;
import com.evcharge.customer.shared.Timestamps;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
class JdbcCouponRepository implements CouponRepository {

    private final JdbcClient jdbc;

    JdbcCouponRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Coupon> findActive(UUID customerId, Instant now) {
        return jdbc.sql("""
                select id, title, description, expires_at from coupon
                where customer_id = :customer and used_at is null and (expires_at is null or expires_at > :now)
                order by created_at desc
                """)
                .param("customer", customerId).param("now", Timestamps.toDb(now))
                .query((rs, rowNum) -> new Coupon(rs.getObject("id", UUID.class), rs.getString("title"),
                        rs.getString("description"), Timestamps.read(rs, "expires_at")))
                .list();
    }
}
