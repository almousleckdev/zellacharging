package com.evcharge.customer.account.application;

import com.evcharge.customer.account.CustomerModeration;
import com.evcharge.customer.account.application.port.AppleTokenRevoker;
import com.evcharge.customer.account.application.port.CustomerRepository;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountClosureService {

    private final PasswordConfirmation passwords;
    private final SocialReauthentication social;
    private final CustomerRepository customers;
    private final CustomerModeration moderation;
    private final AppleTokenRevoker apple;
    private final AuditTrail audit;

    public AccountClosureService(PasswordConfirmation passwords, SocialReauthentication social,
                                 CustomerRepository customers, CustomerModeration moderation,
                                 AppleTokenRevoker apple, AuditTrail audit) {
        this.passwords = passwords;
        this.social = social;
        this.customers = customers;
        this.moderation = moderation;
        this.apple = apple;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void close(UUID customerId, ClosureProof proof) {
        if (customers.hasPassword(customerId)) {
            passwords.confirm(customerId, proof.password() == null ? "" : proof.password());
        } else {
            social.confirm(customerId, proof.provider(), proof.idToken());
        }
        moderation.delete(customerId);
        apple.revoke(proof.authorizationCode());
        audit.record("ACCOUNT_CLOSED", customerId, "customer", null);
    }
}
