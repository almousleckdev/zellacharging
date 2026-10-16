package com.evcharge.customer.wallet.web;

import com.evcharge.customer.wallet.application.TopupService;
import com.evcharge.customer.wallet.application.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    WalletController(WalletService wallets, TopupService topups) {
        this.wallets = wallets;
        this.topups = topups;
    }

    @GetMapping
    WalletResponse balance(@AuthenticationPrincipal Jwt jwt) {
        return WalletResponse.from(wallets.balance(customer(jwt)));
    }

    @GetMapping("/transactions")
    List<LedgerResponse> transactions(@AuthenticationPrincipal Jwt jwt) {
        return wallets.transactions(customer(jwt)).stream().map(LedgerResponse::from).toList();
    }

    @PostMapping("/topups")
    @ResponseStatus(HttpStatus.CREATED)
    TopupResponse topUp(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TopupRequest request,
                        @RequestHeader(name = "Idempotency-Key", required = false) String key) {
        String idempotencyKey = key == null || key.isBlank() ? UUID.randomUUID().toString() : key;
        return TopupResponse.from(topups.create(customer(jwt), request.operator(), request.msisdn(),
                request.amountMinor(), idempotencyKey));
    }

    @GetMapping("/topups/{id}")
    TopupResponse topup(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return TopupResponse.from(topups.get(customer(jwt), id));
    }

    private static UUID customer(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
