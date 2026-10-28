package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPaymentService;
import com.evcharge.customer.admin.domain.AdminAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/payments")
class AdminPaymentController {

    private final AdminPaymentService payments;

    AdminPaymentController(AdminPaymentService payments) {
        this.payments = payments;
    }

    @GetMapping
    PaymentListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                             @RequestParam(required = false) String kind, @RequestParam(required = false) String state,
                             @RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return PaymentListResponse.from(payments.list(admin, country, kind, state, q, page, size));
    }
}
