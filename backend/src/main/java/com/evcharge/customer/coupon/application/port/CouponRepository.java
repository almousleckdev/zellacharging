package com.evcharge.customer.coupon.application.port;

import com.evcharge.customer.coupon.domain.Coupon;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CouponRepository {

    List<Coupon> findActive(UUID customerId, Instant now);
}
