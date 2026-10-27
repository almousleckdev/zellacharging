package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminAccountRepository;
import com.evcharge.customer.admin.application.port.AdminMailer;
import com.evcharge.customer.admin.domain.AdminUser;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
class AdminInvitationSender {

    private final AdminAccountRepository accounts;
    private final AdminMailer mailer;
    private final InvitationTokens tokens;
    private final AdminSettings settings;
    private final Clock clock;

    AdminInvitationSender(AdminAccountRepository accounts, AdminMailer mailer, InvitationTokens tokens,
                          AdminSettings settings, Clock clock) {
        this.accounts = accounts;
        this.mailer = mailer;
        this.tokens = tokens;
        this.settings = settings;
        this.clock = clock;
    }

    boolean send(AdminUser target) {
        String token = tokens.newToken();
        Instant expiresAt = clock.instant().plus(settings.invitationTtl());
        accounts.storeInvitation(target.id(), tokens.hash(token), expiresAt);
        String base = settings.dashboardUrl().replaceAll("/+$", "");
        return mailer.sendInvitation(target.email(), base + "/accept-invite?token=" + token, expiresAt);
    }
}
