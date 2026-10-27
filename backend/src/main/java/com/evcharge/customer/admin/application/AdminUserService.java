package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminRefreshTokenRepository;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.AdminPage;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminUserService {


    private final AdminAccessService access;
    private final AdminAccessRules rules;
    private final AdminTargets targets;
    private final AdminInvitationSender invitations;
    private final AdminRepository admins;
    private final AdminAccountRepository accounts;
    private final AdminRefreshTokenRepository refreshTokens;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminUserService(AdminAccessService access, AdminAccessRules rules, AdminTargets targets,
                            AdminInvitationSender invitations, AdminRepository admins,
                            AdminAccountRepository accounts, AdminRefreshTokenRepository refreshTokens,
                            AdminAudit audit, Clock clock) {
        this.access = access;
        this.rules = rules;
        this.targets = targets;
        this.invitations = invitations;
        this.admins = admins;
        this.accounts = accounts;
        this.refreshTokens = refreshTokens;
        this.audit = audit;
        this.clock = clock;
    }

    public ManagedAdminPage list(AdminAccess actor, String text, String status, int page, int size) {
        access.require(actor, Permission.ADMINS_MANAGE);
        PageWindow window = PageWindow.of(page, size);
        AdminPage found = accounts.search(blankToNull(text), parseStatus(status), window.size(), window.offset());
        return new ManagedAdminPage(found.total(), window.index(), window.size(),
                found.items().stream().map(targets::managed).toList());
    }

    public ManagedAdmin get(AdminAccess actor, UUID id) {
        access.require(actor, Permission.ADMINS_MANAGE);
        return targets.managed(targets.find(id));
    }

    @Transactional
    public InvitationResult invite(AdminAccess actor, InviteCommand command) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AccessCommand definition = rules.validated(command.access());
        String email = command.email().trim().toLowerCase(Locale.ROOT);
        if (admins.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_USED", "Email already used");
        }
        AdminUser created = AdminUser.create(UUID.randomUUID(), email, null, definition.role(), AdminStatus.INVITED,
                definition.permissions(), definition.countries());
        admins.create(created, clock.instant());
        boolean sent = invitations.send(created);
        audit.record(actor.id(), null, "ADMIN_INVITED", created.id().toString(), email + " " + definition.role());
        return new InvitationResult(targets.managed(targets.find(created.id())), sent);
    }

    @Transactional
    public ManagedAdmin update(AdminAccess actor, UUID id, AccessCommand command) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AdminUser target = targets.unprotected(actor, id);
        AccessCommand definition = rules.validated(command);
        accounts.updateAccess(target.id(), definition.role(), definition.permissions(), definition.countries());
        refreshTokens.revokeAllOf(target.id(), clock.instant());
        audit.record(actor.id(), null, "ADMIN_ACCESS_CHANGED", id.toString(),
                definition.role() + " " + definition.countries());
        return targets.managed(targets.find(id));
    }

    @Transactional
    public boolean resendInvitation(AdminAccess actor, UUID id) {
        access.require(actor, Permission.ADMINS_MANAGE);
        AdminUser target = targets.unprotected(actor, id);
        if (target.status() != AdminStatus.INVITED) {
            throw new ApiException(HttpStatus.CONFLICT, "ADMIN_NOT_PENDING", "The invitation is not pending");
        }
        boolean sent = invitations.send(target);
        audit.record(actor.id(), null, "ADMIN_INVITATION_RESENT", id.toString(), null);
        return sent;
    }

    private static AdminStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return AdminStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Unknown status");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
