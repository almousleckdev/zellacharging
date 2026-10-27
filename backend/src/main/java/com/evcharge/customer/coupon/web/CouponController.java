package com.evcharge.customer.coupon.web;

import com.evcharge.customer.coupon.application.CouponService;
import com.evcharge.customer.shared.CustomerPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/coupons")
class CouponController {

    private final CouponService coupons;

    CouponController(CouponService coupons) {
        this.coupons = coupons;
    }

    @GetMapping
    List<CouponResponse> mine(CustomerPrincipal me) {
        return coupons.active(me.id()).stream().map(CouponResponse::from).toList();
    }
}
