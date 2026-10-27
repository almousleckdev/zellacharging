package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminPasswordPolicy;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AdminInvitationService {

    private final AdminAccountRepository accounts;
    private final AdminRepository admins;
    private final AdminPasswordHasher passwords;
    private final InvitationTokens tokens;
    private final AdminLoginService login;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminInvitationService(AdminAccountRepository accounts, AdminRepository admins,
                                  AdminPasswordHasher passwords, InvitationTokens tokens, AdminLoginService login,
                                  AdminAudit audit, Clock clock) {
        this.accounts = accounts;
        this.admins = admins;
        this.passwords = passwords;
        this.tokens = tokens;
        this.login = login;
        this.audit = audit;
        this.clock = clock;
    }

    public String inspect(String token) {
        return pending(token).email();
    }

    @Transactional
    public AdminLoginStep accept(String token, String password) {
        AdminUser invited = pending(token);
        if (!AdminPasswordPolicy.accepts(password)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "Password too weak");
        }
        accounts.completeInvitation(invited.id(), passwords.hash(password));
        audit.record(invited.id(), null, "ADMIN_INVITATION_ACCEPTED", invited.id().toString(), null);
        return login.beginSecondFactor(admins.findById(invited.id()).orElseThrow());
    }

    private AdminUser pending(String token) {
        return accounts.findByInvitation(tokens.hash(token), clock.instant())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "INVITATION_INVALID",
                        "The invitation is invalid or has expired"));
    }
}
