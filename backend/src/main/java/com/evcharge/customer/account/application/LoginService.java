package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class LoginService {

    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final ContactNormalizer contacts;
    private final SessionIssuer sessions;
    private final AccountSettings settings;
    private final Clock clock;

    public LoginService(CustomerRepository customers, PasswordHasher passwords, ContactNormalizer contacts,
                        SessionIssuer sessions, AccountSettings settings, Clock clock) {
        this.customers = customers;
        this.passwords = passwords;
        this.contacts = contacts;
        this.sessions = sessions;
        this.settings = settings;
        this.clock = clock;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthTokens login(String identifier, String password) {
        ContactPoint contact = contacts.resolve(identifier);
        Customer customer = customers.findByContact(contact.channel(), contact.target())
                .orElseThrow(LoginService::invalidCredentials);
        Instant now = clock.instant();
        if (customer.isLockedAt(now)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ACCOUNT_LOCKED", "Too many failed attempts");
        }
        if (!passwords.matches(password, customer.passwordHash())) {
            registerFailure(customer, now);
            throw invalidCredentials();
        }
        if (!customer.canAuthenticate()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_VERIFIED", "Account is not verified");
        }
        customers.clearFailedLogins(customer.id());
        return sessions.issue(customer.id(), UUID.randomUUID());
    }

    private void registerFailure(Customer customer, Instant now) {
        int attempts = customer.failedLoginAttempts() + 1;
        boolean lock = attempts >= settings.security().maxFailedLogins();
        customers.recordFailedLogin(customer.id(), lock ? 0 : attempts,
                lock ? now.plus(settings.security().lockout()) : null);
    }

    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }
}
