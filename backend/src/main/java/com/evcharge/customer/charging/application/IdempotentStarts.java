package com.evcharge.customer.charging.application;

import com.evcharge.customer.charging.application.port.SessionRepository;
import com.evcharge.customer.charging.domain.ChargingSession;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.WalletFunding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
class IdempotentStarts {

    private final SessionRepository sessions;
    private final WalletFunding funding;

    IdempotentStarts(SessionRepository sessions, WalletFunding funding) {
        this.sessions = sessions;
        this.funding = funding;
    }

    Optional<ChargingSession> earlier(UUID customerId, String key, String stationId, int connectorId,
                                      long amountMinor) {
        if (key == null) {
            return Optional.empty();
        }
        return sessions.findByIdempotencyKey(customerId, key).map(found -> {
            boolean same = found.stationId().equals(stationId) && found.connectorId() == connectorId
                    && sameBudget(found.id(), amountMinor);
            if (!same) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "IDEMPOTENCY_KEY_REUSED",
                        "This key was already used for a different request");
            }
            return found;
        });
    }

    private boolean sameBudget(UUID sessionId, long amountMinor) {
        long reserved = funding.reserved(sessionId).minor();
        boolean freeAtMaximum = funding.waived(sessionId) && reserved == funding.startLimits().maximumMinor();
        return reserved == amountMinor || freeAtMaximum;
    }
}
