package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.application.TopupService;
import com.evcharge.customer.wallet.application.WalletService;
import com.evcharge.customer.wallet.application.WithdrawalSettings;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.CustomerPrincipal;
import com.evcharge.customer.shared.FreeCharging;
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
@RequestMapping("/v1/wallet")
class WalletController {

    private final WalletService wallets;
    private final TopupService topups;
    private final WithdrawalSettings withdrawals;
    private final FreeCharging free;

    WalletController(WalletService wallets, TopupService topups, WithdrawalSettings withdrawals,
                     FreeCharging free) {
        this.wallets = wallets;
        this.topups = topups;
        this.withdrawals = withdrawals;
        this.free = free;
    }

    @GetMapping
    WalletResponse balance(CustomerPrincipal me) {
        return WalletResponse.from(wallets.balance(me.id()), withdrawals.enabled(),
                wallets.payoutSource(me.id()), wallets.payoutSources(me.id()), free.allowed(me.id()));
    }

    @GetMapping("/transactions")
    List<LedgerResponse> transactions(CustomerPrincipal me) {
        return wallets.transactions(me.id()).stream().map(LedgerResponse::from).toList();
    }

    @PostMapping("/topups")
    @ResponseStatus(HttpStatus.CREATED)
    TopupResponse topUp(CustomerPrincipal me, @Valid @RequestBody TopupRequest request,
                        @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        if (key == null || key.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED",
                    "An Idempotency-Key header is required");
        }
        return TopupResponse.from(topups.create(me.id(), request.operator(), request.msisdn(),
                request.amountMinor(), key.trim()));
    }

    @GetMapping("/topups/{id}")
    TopupResponse topup(CustomerPrincipal me, @PathVariable UUID id) {
        return TopupResponse.from(topups.get(me.id(), id));
    }
}
