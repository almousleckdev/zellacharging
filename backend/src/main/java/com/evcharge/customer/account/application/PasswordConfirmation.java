package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class PasswordConfirmation {

    private final ProfileService profiles;
    private final PasswordHasher passwords;
    private final FailedLoginPolicy failures;
    private final AuditTrail audit;

    PasswordConfirmation(ProfileService profiles, PasswordHasher passwords, FailedLoginPolicy failures,
                         AuditTrail audit) {
        this.profiles = profiles;
        this.passwords = passwords;
        this.failures = failures;
        this.audit = audit;
    }

    Customer confirm(UUID customerId, String password) {
        Customer customer = profiles.get(customerId);
        failures.requireNotLocked(customer);
        if (!passwords.matches(password, customer.passwordHash())) {
            failures.registerFailure(customerId);
            audit.record("PASSWORD_CONFIRMATION_FAILED", customerId, "customer", null);
            throw new ApiException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD", "The password is incorrect");
        }
        failures.clear(customerId);
        return customer;
    }
}
