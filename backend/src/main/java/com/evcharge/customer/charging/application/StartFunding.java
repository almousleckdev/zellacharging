package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.FreeSundayAccess;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class StartFunding {

    private static final Logger log = LoggerFactory.getLogger(StartFunding.class);

    private final WalletFunding funding;
    private final FreeSundayAccess freeSunday;

    StartFunding(WalletFunding funding, FreeSundayAccess freeSunday) {
        this.funding = funding;
        this.freeSunday = freeSunday;
    }

    long fund(UUID customerId, UUID sessionId, long amountMinor, Runnable record) {
        long budget = reserve(customerId, sessionId, amountMinor);
        try {
            record.run();
        } catch (RuntimeException e) {
            releaseHold(sessionId, e);
            releaseClaim(sessionId, e);
            throw e;
        }
        return budget;
    }

    private long reserve(UUID customerId, UUID sessionId, long amountMinor) {
        if (!claimed(customerId, sessionId)) {
            funding.reserve(customerId, sessionId, amountMinor);
            return amountMinor;
        }
        long budget = freeBudget(amountMinor);
        try {
            funding.reserveFree(customerId, sessionId, budget);
        } catch (RuntimeException e) {
            releaseClaim(sessionId, e);
            throw e;
        }
        if (freeSunday.holdsClaim(sessionId)) {
            return budget;
        }
        log.warn("Free Sunday claim of session {} was released during the start: starting as a paid charge",
                sessionId);
        funding.discardWaived(sessionId);
        funding.reserve(customerId, sessionId, amountMinor);
        return amountMinor;
    }

    private long freeBudget(long requested) {
        StartLimits limits = funding.startLimits();
        boolean valid = requested >= limits.minimumMinor() && requested <= limits.maximumMinor();
        return valid ? limits.maximumMinor() : requested;
    }

    private boolean claimed(UUID customerId, UUID sessionId) {
        try {
            return freeSunday.claimFor(customerId, sessionId);
        } catch (RuntimeException e) {
            log.warn("Free Sunday claim for session {} failed ({}): starting as a paid charge", sessionId,
                    e.getClass().getSimpleName());
            releaseClaim(sessionId, e);
            return false;
        }
    }

    private void releaseHold(UUID sessionId, RuntimeException cause) {
        try {
            funding.release(sessionId);
        } catch (RuntimeException e) {
            cause.addSuppressed(e);
        }
    }

    private void releaseClaim(UUID sessionId, RuntimeException cause) {
        try {
            freeSunday.release(sessionId);
        } catch (RuntimeException e) {
            cause.addSuppressed(e);
        }
    }
}
