package com.evcharge.customer.coupon.web;

import com.evcharge.customer.coupon.application.CouponService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/coupons")
class CouponController {

    private final CouponService coupons;

    CouponController(CouponService coupons) {
        this.coupons = coupons;
    }

    @GetMapping
    List<CouponResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return coupons.active(UUID.fromString(jwt.getSubject())).stream().map(CouponResponse::from).toList();
    }
}
