package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminWithdrawalLog;
import com.evcharge.customer.admin.application.AdminWithdrawalService;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.shared.LogFilter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/withdrawals")
class AdminWithdrawalController {

    private final AdminWithdrawalService withdrawals;
    private final AdminWithdrawalLog log;

    AdminWithdrawalController(AdminWithdrawalService withdrawals, AdminWithdrawalLog log) {
        this.withdrawals = withdrawals;
        this.log = log;
    }

    @GetMapping
    PaymentListResponse list(AdminAccess admin, @RequestParam(required = false) String country,
                             @RequestParam(required = false) String state, @RequestParam(required = false) String q,
                             @RequestParam(required = false) Instant from, @RequestParam(required = false) Instant to,
                             @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return PaymentListResponse.from(log.list(admin, country, state, q, new LogFilter(from, to, null), sort, page,
                size));
    }

    @GetMapping("/{country}/{id}")
    PaymentDetailResponse detail(AdminAccess admin, @PathVariable String country, @PathVariable UUID id) {
        return PaymentDetailResponse.from(log.detail(admin, country, id));
    }

    @PostMapping("/{country}/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void approve(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                 @Valid @RequestBody(required = false) ApproveWithdrawalRequest request) {
        withdrawals.approve(admin, country, id, request == null ? null : request.note());
    }

    @PostMapping("/{country}/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reject(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                @Valid @RequestBody RejectWithdrawalRequest request) {
        withdrawals.reject(admin, country, id, request.note());
    }

    @PostMapping("/{country}/{id}/refund")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void refund(AdminAccess admin, @PathVariable String country, @PathVariable UUID id,
                @Valid @RequestBody RefundWithdrawalRequest request) {
        withdrawals.refund(admin, country, id, request.note());
    }
}
