package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.IdTagRepository;
import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.charging.domain.RemoteSession;
import com.evcharge.customer.charging.domain.SessionState;
import com.evcharge.customer.wallet.StartLimits;
import com.evcharge.customer.wallet.WalletFunding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.OptionalLong;
import java.util.UUID;

@Component
public class OrphanChargeAdopter {

    private static final Logger log = LoggerFactory.getLogger(OrphanChargeAdopter.class);

    private final IdTagRepository idTags;
    private final SessionRepository sessions;
    private final WalletFunding funding;
    private final SessionSettlement settlement;
    private final Clock clock;

    public OrphanChargeAdopter(IdTagRepository idTags, SessionRepository sessions, WalletFunding funding,
                               SessionSettlement settlement, Clock clock) {
        this.idTags = idTags;
        this.sessions = sessions;
        this.funding = funding;
        this.settlement = settlement;
        this.clock = clock;
    }

    public boolean adopt(RemoteSession remote) {
        UUID customer = idTags.customerOf(remote.idTag()).orElse(null);
        if (customer == null || sessions.findLive(customer).isPresent()) {
            return false;
        }
        OptionalLong budget = budgetOf(customer);
        if (budget.isEmpty()) {
            return false;
        }
        UUID id = UUID.randomUUID();
        Instant now = clock.instant();
        ChargingSession requested = ChargingSession.requested(id, customer, remote.stationId(),
                remote.connectorId(), remote.idTag(), now);
        if (!hold(requested, budget.getAsLong(), remote.transactionId())) {
            return false;
        }
        ChargingSession active = requested.withRemote(remote, SessionState.ACTIVE, now);
        sessions.update(active);
        settlement.onChange(requested, active);
        log.error("ALERT transaction {} had no session: adopted it as session {} for its customer, funds held",
                remote.transactionId(), id);
        return true;
    }

    private boolean hold(ChargingSession session, long budget, int transaction) {
        try {
            funding.reserve(session.customerId(), session.id(), budget);
            sessions.insert(session, "adopt:" + transaction);
            return true;
        } catch (RuntimeException e) {
            funding.release(session.id());
            log.warn("Transaction {} could not be adopted", transaction, e);
            return false;
        }
    }

    private OptionalLong budgetOf(UUID customer) {
        StartLimits limits = funding.startLimits();
        long amount = Math.min(funding.available(customer).minor(), limits.maximumMinor());
        return amount >= limits.minimumMinor() ? OptionalLong.of(amount) : OptionalLong.empty();
    }
}
