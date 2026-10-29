package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminPaymentService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.LogFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

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
                             @RequestParam(required = false) String q, @RequestParam(required = false) Instant from,
                             @RequestParam(required = false) Instant to, @RequestParam(required = false) UUID customerId, @RequestParam(required = false) String sort,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return PaymentListResponse.from(payments.list(admin, country, kind, state, q, new LogFilter(from, to, customerId), sort, page, size));
    }

    @GetMapping("/by-customer")
    PaymentDigestResponse byCustomer(AdminAccess admin, @RequestParam(required = false) String country,
                                     @RequestParam(required = false) String kind,
                                     @RequestParam(required = false) String state,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(required = false) Instant from,
                                     @RequestParam(required = false) Instant to,
                                     @RequestParam(required = false) String sort,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return PaymentDigestResponse.from(payments.byCustomer(admin, country, kind, state, q,
                new LogFilter(from, to, null), sort, page, size));
    }

    @GetMapping("/{country}/{kind}/{id}")
    PaymentDetailResponse detail(AdminAccess admin, @PathVariable String country, @PathVariable String kind,
                                 @PathVariable UUID id) {
        return PaymentDetailResponse.from(payments.detail(admin, country, kind, id));
    }
}
