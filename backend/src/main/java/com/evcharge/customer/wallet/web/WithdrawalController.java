package com.evcharge.customer.wallet.web;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerPrincipal;
import com.evcharge.customer.wallet.application.WithdrawalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/wallet/withdrawals")
class WithdrawalController {

    private final WithdrawalService withdrawals;

    WithdrawalController(WithdrawalService withdrawals) {
        this.withdrawals = withdrawals;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    WithdrawalResponse request(CustomerPrincipal me, @Valid @RequestBody WithdrawalRequest request,
                               @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        if (key == null || key.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED",
                    "An Idempotency-Key header is required");
        }
        return WithdrawalResponse.from(withdrawals.request(me.id(), request.operator(), request.amountMinor(),
                request.password(), key.trim()));
    }

    @GetMapping
    List<WithdrawalResponse> recent(CustomerPrincipal me) {
        return withdrawals.recent(me.id()).stream().map(WithdrawalResponse::from).toList();
    }

    @GetMapping("/{id}")
    WithdrawalResponse get(CustomerPrincipal me, @PathVariable UUID id) {
        return WithdrawalResponse.from(withdrawals.get(me.id(), id));
    }
}
