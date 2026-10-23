package com.evcharge.customer.coupon.web;

import com.evcharge.customer.coupon.domain.Coupon;

import java.time.Instant;
import java.util.UUID;

record CouponResponse(UUID id, String title, String description, Instant expiresAt) {

    static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.id(), coupon.title(), coupon.description(), coupon.expiresAt());
    }
}
