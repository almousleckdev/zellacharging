package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminRepository admins;
    private final AdminPasswordHasher passwords;
    private final AdminSettings settings;
    private final Clock clock;

    AdminBootstrap(AdminRepository admins, AdminPasswordHasher passwords, AdminSettings settings, Clock clock) {
        this.admins = admins;
        this.passwords = passwords;
        this.settings = settings;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(settings.bootstrapEmail()) || isBlank(settings.bootstrapPassword())
                || admins.existsSuperAdmin()) {
            return;
        }
        String email = settings.bootstrapEmail().trim().toLowerCase(Locale.ROOT);
        admins.create(AdminUser.create(UUID.randomUUID(), email, passwords.hash(settings.bootstrapPassword()),
                AdminRole.SUPER_ADMIN, AdminStatus.ACTIVE, Set.of(), Set.of()), clock.instant());
        LOG.info("Super admin {} created from bootstrap settings", email);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
