package com.evcharge.customer.wallet.web;

import com.evcharge.customer.shared.CustomerPrincipal;
import com.evcharge.customer.wallet.application.WalletHistory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/wallet/transactions")
class WalletHistoryController {

    private final WalletHistory history;

    WalletHistoryController(WalletHistory history) {
        this.history = history;
    }

    @GetMapping("/{id}")
    LedgerDetailResponse detail(CustomerPrincipal me, @PathVariable UUID id) {
        return LedgerDetailResponse.from(history.detail(me.id(), id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void hide(CustomerPrincipal me, @PathVariable UUID id) {
        history.hide(me.id(), id);
    }
}
