package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.TopupRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import com.evcharge.customer.wallet.domain.Topup;
import com.evcharge.customer.wallet.domain.TopupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Component
class TopupCredit {

    private static final Logger log = LoggerFactory.getLogger(TopupCredit.class);

    private final WalletSettings settings;
    private final TopupRepository topups;
    private final WalletRepository wallets;
    private final LedgerRepository ledger;
    private final Clock clock;
    private final AuditTrail audit;

    TopupCredit(WalletSettings settings, TopupRepository topups, WalletRepository wallets,
                LedgerRepository ledger, Clock clock, AuditTrail audit) {
        this.settings = settings;
        this.topups = topups;
        this.wallets = wallets;
        this.ledger = ledger;
        this.clock = clock;
        this.audit = audit;
    }

    @Transactional
    public void succeed(Topup topup, String providerRef, long confirmedMinor) {
        long credited = Math.min(confirmedMinor, topup.amountMinor());
        if (credited <= 0) {
            return;
        }
        boolean moved = topups.transition(topup.id(), TopupState.PENDING, TopupState.SUCCEEDED, providerRef, null)
                || topups.transition(topup.id(), TopupState.EXPIRED, TopupState.SUCCEEDED, providerRef, null);
        if (!moved) {
            return;
        }
        if (credited < topup.amountMinor()) {
            log.error("Payment {} confirmed {} for a requested {}: credited the confirmed amount only",
                    topup.reference(), confirmedMinor, topup.amountMinor());
        }
        wallets.ensure(topup.customerId(), settings.currency(), settings.decimals());
        wallets.lock(topup.customerId());
        wallets.apply(topup.customerId(), credited, 0);
        ledger.append(new LedgerEntry(UUID.randomUUID(), topup.customerId(), LedgerType.TOPUP, credited, null,
                topup.id(), "topup:" + topup.id(), clock.instant()));
        audit.record("WALLET_CREDITED", topup.customerId(), topup.reference(), Long.toString(credited));
    }
}
