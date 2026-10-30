package com.evcharge.customer.account.application;

import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.account.application.port.PasswordHasher;
import com.evcharge.customer.account.application.port.RefreshTokenRepository;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import com.evcharge.customer.shared.NotificationType;
import com.evcharge.customer.shared.Notifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordService {

    private final PasswordConfirmation confirmation;
    private final CustomerRepository customers;
    private final PasswordHasher passwords;
    private final RefreshTokenRepository refreshTokens;
    private final Clock clock;
    private final AuditTrail audit;
    private final Notifier notifier;

    public PasswordService(PasswordConfirmation confirmation, CustomerRepository customers,
                           PasswordHasher passwords, RefreshTokenRepository refreshTokens, Clock clock,
                           AuditTrail audit, Notifier notifier) {
        this.confirmation = confirmation;
        this.customers = customers;
        this.passwords = passwords;
        this.refreshTokens = refreshTokens;
        this.clock = clock;
        this.audit = audit;
        this.notifier = notifier;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void change(UUID customerId, String currentPassword, String newPassword) {
        confirmation.confirm(customerId, currentPassword);
        customers.updatePasswordHash(customerId, passwords.hash(newPassword));
        refreshTokens.revokeAllOf(customerId, clock.instant());
        audit.record("PASSWORD_CHANGED", customerId, "customer", null);
        notifier.notify(customerId, NotificationType.PASSWORD_CHANGED, "password:" + UUID.randomUUID(),
                Map.of("screen", "settings"));
    }
}
