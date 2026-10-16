package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.RefreshToken;
import com.evcharge.customer.account.domain.Sha256;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class RefreshSessionService {

    private final RefreshTokenRepository refreshTokens;
    private final CustomerRepository customers;
    private final SessionIssuer sessions;
    private final Clock clock;

    public RefreshSessionService(RefreshTokenRepository refreshTokens, CustomerRepository customers,
                          SessionIssuer sessions, Clock clock) {
        this.refreshTokens = refreshTokens;
        this.customers = customers;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthTokens refresh(String rawToken) {
        Instant now = clock.instant();
        RefreshToken token = refreshTokens.findByHash(Sha256.hex(rawToken)).orElseThrow(RefreshSessionService::invalid);
        if (token.isConsumed()) {
            refreshTokens.revokeFamily(token.familyId(), now);
            throw invalid();
        }
        if (token.isExpiredAt(now) || !refreshTokens.markUsed(token.id(), now)) {
            throw invalid();
        }
        customers.findById(token.customerId()).filter(customer -> customer.canAuthenticate())
                .orElseThrow(RefreshSessionService::invalid);
        return sessions.issue(token.customerId(), token.familyId());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByHash(Sha256.hex(rawToken))
                .ifPresent(token -> refreshTokens.revokeFamily(token.familyId(), clock.instant()));
    }

    private static ApiException invalid() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Invalid refresh token");
    }
}
