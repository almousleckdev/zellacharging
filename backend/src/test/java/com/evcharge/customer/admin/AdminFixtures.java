package com.evcharge.customer.admin;

import com.evcharge.customer.admin.application.port.AdminPasswordHasher;
import com.evcharge.customer.admin.application.port.AdminRepository;
import com.evcharge.customer.admin.application.port.AdminTokenIssuer;
import com.evcharge.customer.admin.domain.AdminRole;
import com.evcharge.customer.admin.domain.AdminStatus;
import com.evcharge.customer.admin.domain.AdminUser;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class AdminFixtures {

    public static final String PASSWORD = "Str0ng-Pass!";

    public record Created(UUID id, String email, String accessToken) {
    }

    private final AdminRepository admins;
    private final AdminPasswordHasher passwords;
    private final AdminTokenIssuer tokens;

    public AdminFixtures(AdminRepository admins, AdminPasswordHasher passwords, AdminTokenIssuer tokens) {
        this.admins = admins;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    public Created create(AdminRole role, Set<String> countries) {
        return store(role, countries, true);
    }

    public Created createWithoutProfile(AdminRole role, Set<String> countries) {
        return store(role, countries, false);
    }

    private Created store(AdminRole role, Set<String> countries, boolean complete) {
        UUID id = UUID.randomUUID();
        String email = id + "@zella.test";
        AdminUser user = AdminUser.create(id, email, passwords.hash(PASSWORD), role, AdminStatus.ACTIVE,
                role.permissions(), countries);
        admins.create(complete ? user.withName("Test", "Admin") : user, Instant.now());
        return new Created(id, email, tokens.issueAccess(admins.findById(id).orElseThrow()));
    }
}
