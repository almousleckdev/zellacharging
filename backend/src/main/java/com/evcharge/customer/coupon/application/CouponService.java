package com.evcharge.customer.coupon.application;

import com.evcharge.customer.coupon.application.port.CouponRepository;
import com.evcharge.customer.coupon.domain.Coupon;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
public class CouponService {

    private final CouponRepository coupons;
    private final Clock clock;

    public CouponService(CouponRepository coupons, Clock clock) {
        this.coupons = coupons;
        this.clock = clock;
    }

    public List<Coupon> active(UUID customerId) {
        return coupons.findActive(customerId, clock.instant());
    }
}
