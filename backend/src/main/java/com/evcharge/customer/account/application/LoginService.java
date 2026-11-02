package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class LoginService {

    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final ContactNormalizer contacts;
    private final SessionIssuer sessions;
    private final FailedLoginPolicy failures;
    private final AuditTrail audit;

    public LoginService(CustomerRepository customers, PasswordHasher passwords, ContactNormalizer contacts,
                        SessionIssuer sessions, FailedLoginPolicy failures, AuditTrail audit) {
        this.customers = customers;
        this.passwords = passwords;
        this.contacts = contacts;
        this.sessions = sessions;
        this.failures = failures;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthTokens login(String identifier, String password) {
        ContactPoint contact = contacts.resolve(identifier);
        Customer customer = customers.findByContact(contact.channel(), contact.target())
                .orElseThrow(LoginService::invalidCredentials);
        failures.requireNotLocked(customer);
        if (!passwords.matches(password, customer.passwordHash())) {
            failures.registerFailure(customer.id());
            audit.record("LOGIN_FAILED", customer.id(), "customer", null);
            throw invalidCredentials();
        }
        if (customer.status() == CustomerStatus.BLOCKED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED", "Account is suspended");
        }
        if (!customer.canAuthenticate()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_VERIFIED", "Account is not verified");
        }
        failures.clear(customer.id());
        audit.record("LOGIN_SUCCEEDED", customer.id(), "customer", null);
        return sessions.start(customer.id(), customer.country());
    }

    private static ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials");
    }
}
