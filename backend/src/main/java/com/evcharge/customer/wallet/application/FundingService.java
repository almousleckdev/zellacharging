package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.FreeCharging;
import com.evcharge.customer.shared.Money;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import com.evcharge.customer.wallet.application.port.HoldRepository;
import com.evcharge.customer.wallet.application.port.WalletRepository;
import com.evcharge.customer.wallet.domain.FundingHold;
import com.evcharge.customer.wallet.domain.HoldState;
import com.evcharge.customer.wallet.domain.WalletBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;

@Service
public class FundingService implements WalletFunding {

    private static final Logger log = LoggerFactory.getLogger(FundingService.class);

    private final WalletSettings settings;
    private final WalletRepository wallets;
    private final HoldRepository holds;
    private final HoldCloser closer;
    private final FreeCharging free;

    public FundingService(WalletSettings settings, WalletRepository wallets, HoldRepository holds,
                          HoldCloser closer, FreeCharging free) {
        this.settings = settings;
        this.wallets = wallets;
        this.holds = holds;
        this.closer = closer;
        this.free = free;
    }

    @Override
    @Transactional
    public Money reserve(UUID customerId, UUID sessionId, long amountMinor) {
        return hold(customerId, sessionId, amountMinor, () -> free.allowed(customerId));
    }

    @Override
    @Transactional
    public Money reserveFree(UUID customerId, UUID sessionId, long amountMinor) {
        return hold(customerId, sessionId, amountMinor, () -> true);
    }

    private Money hold(UUID customerId, UUID sessionId, long amountMinor, BooleanSupplier waiver) {
        wallets.ensure(customerId, settings.currency(), settings.decimals());
        WalletBalance wallet = wallets.lock(customerId);
        Optional<FundingHold> existing = holds.lock(sessionId);
        if (existing.isPresent()) {
            return money(existing.get().reservedMinor());
        }
        if (amountMinor < settings.minimumStartMinor() || amountMinor > settings.maximumStartMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", "Amount is outside the allowed range");
        }
        boolean waived = waiver.getAsBoolean();
        if (!waived && wallet.availableMinor() < amountMinor) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_FUNDS",
                    "Top up your wallet to start charging");
        }
        holds.insert(new FundingHold(sessionId, customerId, amountMinor, HoldState.OPEN, waived));
        if (!waived) {
            closer.reserved(customerId, sessionId, amountMinor);
        }
        return money(amountMinor);
    }

    @Override
    @Transactional
    public void settle(UUID sessionId, Long costMinor, String currency) {
        Optional<FundingHold> hold = holds.lock(sessionId).filter(FundingHold::isOpen);
        if (hold.isEmpty()) {
            return;
        }
        if (costMinor == null) {
            log.error("ALERT hold {} closed without a cost: nothing is charged, flagged for review", sessionId);
            closer.close(hold.get(), 0, HoldState.REVIEW);
            return;
        }
        boolean comparable = settings.currency().equals(currency);
        long charge = comparable && !hold.get().waived() ? Math.min(costMinor, hold.get().reservedMinor()) : 0;
        if (!comparable) {
            log.warn("Session {} priced in {} but wallet is {}: flagged for review", sessionId, currency,
                    settings.currency());
        }
        closer.close(hold.get(), charge, comparable ? (charge > 0 ? HoldState.SETTLED : HoldState.RELEASED)
                : HoldState.REVIEW);
    }

    @Override
    @Transactional
    public void release(UUID sessionId) {
        holds.lock(sessionId).filter(FundingHold::isOpen).ifPresent(hold -> closer.close(hold, 0, HoldState.RELEASED));
    }

    @Override
    @Transactional
    public void discardWaived(UUID sessionId) {
        holds.lock(sessionId).filter(hold -> hold.isOpen() && hold.waived())
                .ifPresent(hold -> holds.delete(sessionId));
    }

    @Override
    public List<UUID> openHoldSessions() {
        return holds.openSessionIds();
    }

    @Override
    @Transactional
    public Money available(UUID customerId) {
        if (free.allowed(customerId)) {
            return money(settings.maximumStartMinor());
        }
        wallets.ensure(customerId, settings.currency(), settings.decimals());
        return money(Math.max(0, wallets.read(customerId).availableMinor()));
    }

    @Override
    public StartLimits startLimits() {
        return new StartLimits(settings.minimumStartMinor(), settings.maximumStartMinor(), settings.currency(),
                settings.decimals());
    }

    @Override
    public Money reserved(UUID sessionId) {
        return holds.find(sessionId).map(hold -> money(hold.reservedMinor())).orElse(money(0));
    }

    @Override
    public Optional<Long> captured(UUID sessionId) {
        return holds.captured(sessionId);
    }

    @Override
    public boolean waived(UUID sessionId) {
        return holds.find(sessionId).map(FundingHold::waived).orElse(false);
    }

    private Money money(long minor) {
        return new Money(minor, settings.currency(), settings.decimals());
    }
}
