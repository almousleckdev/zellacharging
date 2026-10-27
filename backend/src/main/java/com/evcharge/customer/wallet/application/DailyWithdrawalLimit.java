package com.evcharge.customer.wallet.application;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.wallet.application.port.WithdrawalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Component
class DailyWithdrawalLimit {

    private static final Duration WINDOW = Duration.ofHours(24);

    private final WithdrawalRepository withdrawals;
    private final WithdrawalSettings settings;
    private final Clock clock;

    DailyWithdrawalLimit(WithdrawalRepository withdrawals, WithdrawalSettings settings, Clock clock) {
        this.withdrawals = withdrawals;
        this.settings = settings;
        this.clock = clock;
    }

    void requireNotExceeded(UUID customerId) {
        long requested = withdrawals.requestedSince(customerId, clock.instant().minus(WINDOW));
        if (requested > settings.dailyMaximumMinor()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WITHDRAWAL_LIMIT_REACHED",
                    "The daily withdrawal limit is reached");
        }
    }
}
