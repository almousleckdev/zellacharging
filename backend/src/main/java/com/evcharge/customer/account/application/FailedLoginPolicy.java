package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
class FailedLoginPolicy {

    private final CustomerRepository customers;
    private final SecuritySettings settings;
    private final Clock clock;

    FailedLoginPolicy(CustomerRepository customers, AccountSettings settings, Clock clock) {
        this.customers = customers;
        this.settings = settings.security();
        this.clock = clock;
    }

    void requireNotLocked(Customer customer) {
        if (customer.isLockedAt(clock.instant())) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ACCOUNT_LOCKED", "Too many failed attempts");
        }
    }

    void registerFailure(UUID customerId) {
        customers.registerFailedLogin(customerId, settings.maxFailedLogins(),
                clock.instant().plus(settings.lockout()));
    }

    void clear(UUID customerId) {
        customers.clearFailedLogins(customerId);
    }
}
