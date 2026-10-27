package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class AdminLifecycleService {

    private final AdminAccessService access;
    private final AdminTargets targets;
    private final AdminAccountRepository accounts;
    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminLifecycleService(AdminAccessService access, AdminTargets targets, AdminAccountRepository accounts,
                                 AdminRefreshTokenRepository refreshTokens, AdminAudit audit, Clock clock) {
        this.access = access;
        this.targets = targets;
        this.accounts = accounts;
        this.refreshTokens = refreshTokens;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public ManagedAdmin disable(AdminAccess actor, UUID id) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AdminUser target = targets.unprotected(actor, id);
        accounts.updateStatus(target.id(), AdminStatus.DISABLED);
        refreshTokens.revokeAllOf(target.id(), clock.instant());
        audit.record(actor.id(), null, "ADMIN_DISABLED", id.toString(), null);
        return targets.managed(targets.find(id));
    }

    @Transactional
    public ManagedAdmin enable(AdminAccess actor, UUID id) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AdminUser target = targets.unprotected(actor, id);
        accounts.updateStatus(target.id(), target.passwordHash() == null ? AdminStatus.INVITED : AdminStatus.ACTIVE);
        audit.record(actor.id(), null, "ADMIN_ENABLED", id.toString(), null);
        return targets.managed(targets.find(id));
    }

    @Transactional
    public void deleteInvited(AdminAccess actor, UUID id) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AdminUser target = targets.unprotected(actor, id);
        if (!accounts.deleteInvited(target.id())) {
            throw new ApiException(HttpStatus.CONFLICT, "ADMIN_NOT_DELETABLE",
                    "Only pending invitations can be deleted");
        }
        audit.record(actor.id(), null, "ADMIN_DELETED", id.toString(), target.email());
    }
}
