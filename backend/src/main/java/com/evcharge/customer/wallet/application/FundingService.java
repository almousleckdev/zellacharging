package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.WalletFunding;
import com.evcharge.customer.wallet.application.port.HoldRepository;
import com.evcharge.customer.wallet.application.port.LedgerRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.FundingHold;
import com.evcharge.customer.wallet.domain.HoldState;
import com.evcharge.customer.wallet.domain.LedgerEntry;
import com.evcharge.customer.wallet.domain.LedgerType;
import com.evcharge.customer.wallet.domain.WalletBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FundingService implements WalletFunding {

    private static final Logger log = LoggerFactory.getLogger(FundingService.class);

    private final WalletSettings settings;
    private final WalletRepository wallets;
    private final HoldRepository holds;
    private final LedgerRepository ledger;
    private final Clock clock;

    public FundingService(WalletSettings settings, WalletRepository wallets, HoldRepository holds,
                          LedgerRepository ledger, Clock clock) {
        this.settings = settings;
        this.wallets = wallets;
        this.holds = holds;
        this.ledger = ledger;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Money reserve(UUID customerId, UUID sessionId, long amountMinor) {
        wallets.ensure(customerId, settings.currency(), settings.decimals());
        WalletBalance wallet = wallets.lock(customerId);
        Optional<FundingHold> existing = holds.lock(sessionId);
        if (existing.isPresent()) {
            return money(existing.get().reservedMinor());
        }
        if (amountMinor < settings.minimumStartMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is below the minimum");
        }
        if (wallet.availableMinor() < amountMinor) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_FUNDS",
                    "Top up your wallet to start charging");
        }
        holds.insert(new FundingHold(sessionId, customerId, amountMinor, HoldState.OPEN));
        wallets.apply(customerId, 0, amountMinor);
        record(customerId, LedgerType.RESERVE, amountMinor, sessionId, "reserve:" + sessionId);
        return money(amountMinor);
    }

    @Override
    @Transactional
    public void settle(UUID sessionId, Long costMinor, String currency) {
        Optional<FundingHold> hold = holds.lock(sessionId).filter(FundingHold::isOpen);
        if (hold.isEmpty()) {
            return;
        }
        boolean comparable = costMinor == null || settings.currency().equals(currency);
        long charge = costMinor == null || !comparable ? 0 : Math.min(costMinor, hold.get().reservedMinor());
        if (!comparable) {
            log.warn("Session {} priced in {} but wallet is {}: flagged for review", sessionId, currency,
                    settings.currency());
        }
        close(hold.get(), charge, comparable ? (charge > 0 ? HoldState.SETTLED : HoldState.RELEASED)
                : HoldState.REVIEW);
    }

    @Override
    @Transactional
    public void release(UUID sessionId) {
        holds.lock(sessionId).filter(FundingHold::isOpen).ifPresent(hold -> close(hold, 0, HoldState.RELEASED));
    }

    @Override
    public List<UUID> openHoldSessions() {
        return holds.openSessionIds();
    }

    @Override
    public Money reserved(UUID sessionId) {
        return holds.find(sessionId).map(hold -> money(hold.reservedMinor())).orElse(money(0));
    }

    private void close(FundingHold hold, long charge, HoldState state) {
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

    private Money money(long minor) {
        return new Money(minor, settings.currency(), settings.decimals());
    }
}
