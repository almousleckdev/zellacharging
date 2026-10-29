package com.evcharge.customer.account.application;

import com.evcharge.customer.account.CustomerModeration;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountClosureService {

    private final PasswordConfirmation passwords;
    private final CustomerModeration moderation;
    private final AuditTrail audit;

    public AccountClosureService(PasswordConfirmation passwords, CustomerModeration moderation, AuditTrail audit) {
        this.passwords = passwords;
        this.moderation = moderation;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void close(UUID customerId, String password) {
        passwords.confirm(customerId, password);
        moderation.delete(customerId);
        audit.record("ACCOUNT_CLOSED", customerId, "customer", null);
    }
}
