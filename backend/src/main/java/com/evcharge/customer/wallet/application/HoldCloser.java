package com.evcharge.customer.wallet.application;

import com.evcharge.customer.wallet.application.port.HoldRepository;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.FundingHold;
import com.evcharge.customer.wallet.domain.HoldState;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
class HoldCloser {

    private final WalletRepository wallets;
    private final HoldRepository holds;
    private final LedgerRepository ledger;
    private final Clock clock;

    HoldCloser(WalletRepository wallets, HoldRepository holds, LedgerRepository ledger, Clock clock) {
        this.wallets = wallets;
        this.holds = holds;
        this.ledger = ledger;
        this.clock = clock;
    }

    void reserved(UUID customerId, UUID sessionId, long amountMinor) {
        wallets.apply(customerId, 0, amountMinor);
        record(customerId, LedgerType.RESERVE, amountMinor, sessionId, "reserve:" + sessionId);
    }

    void close(FundingHold hold, long charge, HoldState state) {
        if (hold.waived()) {
            holds.close(hold.sessionId(), state, 0);
            return;
        }
        wallets.lock(hold.customerId());
        wallets.apply(hold.customerId(), -charge, -hold.reservedMinor());
        if (charge > 0) {
            record(hold.customerId(), LedgerType.CAPTURE, charge, hold.sessionId(), "capture:" + hold.sessionId());
        }
        long unused = hold.reservedMinor() - charge;
        if (unused > 0) {
            record(hold.customerId(), LedgerType.RELEASE, unused, hold.sessionId(), "release:" + hold.sessionId());
        }
        holds.close(hold.sessionId(), state, charge);
    }

    private void record(UUID customerId, LedgerType type, long amount, UUID sessionId, String key) {
        ledger.append(new LedgerEntry(UUID.randomUUID(), customerId, type, amount, sessionId, null, key,
                clock.instant()));
    }
}
