package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminWithdrawalService;
import com.evcharge.customer.admin.domain.AdminAccess;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/withdrawals")
class AdminWithdrawalController {

    private final AdminWithdrawalService withdrawals;

    AdminWithdrawalController(AdminWithdrawalService withdrawals) {
        this.withdrawals = withdrawals;
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
}
