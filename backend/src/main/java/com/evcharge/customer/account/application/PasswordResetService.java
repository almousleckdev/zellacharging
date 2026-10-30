package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.account.domain.Channel;
import com.evcharge.customer.account.domain.ContactPoint;
import com.evcharge.customer.account.domain.Customer;
import com.evcharge.customer.account.domain.CustomerStatus;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final CustomerRepository customers;
    private final VerificationService verification;
    private final ContactNormalizer contacts;
    private final PasswordHasher passwords;
    private final RefreshTokenRepository refreshTokens;
    private final Clock clock;
    private final AuditTrail audit;
    private final Notifier notifier;

    public PasswordResetService(CustomerRepository customers, VerificationService verification,
                                ContactNormalizer contacts, PasswordHasher passwords,
                                RefreshTokenRepository refreshTokens, Clock clock, AuditTrail audit,
                                Notifier notifier) {
        this.customers = customers;
        this.verification = verification;
        this.contacts = contacts;
        this.passwords = passwords;
        this.refreshTokens = refreshTokens;
        this.clock = clock;
        this.audit = audit;
        this.notifier = notifier;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void request(String rawEmail) {
        ContactPoint email = new ContactPoint(Channel.EMAIL, contacts.email(rawEmail));
        customers.findByContact(Channel.EMAIL, email.target()).filter(this::eligible).ifPresent(customer -> {
            try {
                verification.reissue(customer, email, Channel.PASSWORD_RESET);
            } catch (ApiException tooSoon) {
                if (tooSoon.status() != HttpStatus.TOO_MANY_REQUESTS) {
                    throw tooSoon;
                }
            }
        });
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void reset(String rawEmail, String code, String newPassword) {
        String email = contacts.email(rawEmail);
        Customer customer = customers.findByContact(Channel.EMAIL, email).filter(this::eligible)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CODE", "Invalid or expired code"));
        verification.confirm(customer, Channel.PASSWORD_RESET, code);
        customers.updatePasswordHash(customer.id(), passwords.hash(newPassword));
        customers.clearFailedLogins(customer.id());
        refreshTokens.revokeAllOf(customer.id(), clock.instant());
        audit.record("PASSWORD_RESET", customer.id(), "customer", null);
        notifier.notify(customer.id(), NotificationType.PASSWORD_CHANGED, "password:" + UUID.randomUUID(),
                Map.of("screen", "settings"));
    }

    private boolean eligible(Customer customer) {
        return customer.status() == CustomerStatus.ACTIVE && customers.isVerified(customer.id(), Channel.EMAIL);
    }
}
