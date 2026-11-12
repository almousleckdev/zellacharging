package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.ComplimentaryAccess;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminComplimentaryService {

    private final AdminAccessService access;
    private final ComplimentaryAccess complimentary;
    private final AdminAudit audit;
    private final Clock clock;

    public AdminComplimentaryService(AdminAccessService access, ComplimentaryAccess complimentary,
                                     AdminAudit audit, Clock clock) {
        this.access = access;
        this.complimentary = complimentary;
        this.audit = audit;
        this.clock = clock;
    }

    public List<CountryComplimentary> list(AdminAccess admin, String country) {
        List<CountryComplimentary> items = new ArrayList<>();
        for (String code : access.scope(admin, Permission.COMPLIMENTARY_MANAGE, country)) {
            TenantContext.callAs(code, complimentary::all).forEach(grant -> items.add(new CountryComplimentary(code, grant)));
        }
        return items;
    }

    public void grant(AdminAccess admin, String country, UUID customerId, String reason, Instant expiresAt) {
        String code = AdminCountries.single(access.scope(admin, Permission.COMPLIMENTARY_MANAGE, country));
        if (reason == null || reason.isBlank() || (expiresAt != null && !expiresAt.isAfter(clock.instant()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_COMPLIMENTARY",
                    "A reason and a future end date are required");
        }
        TenantContext.runAs(code, () -> complimentary.grant(customerId, admin.id(), reason.trim(), expiresAt));
        audit.record(admin.id(), code, "COMPLIMENTARY_GRANTED", customerId.toString(), reason.trim());
    }

    public void revoke(AdminAccess admin, String country, UUID customerId) {
        String code = AdminCountries.single(access.scope(admin, Permission.COMPLIMENTARY_MANAGE, country));
        boolean removed = TenantContext.callAs(code, () -> complimentary.revoke(customerId));
        if (removed) {
            audit.record(admin.id(), code, "COMPLIMENTARY_REVOKED", customerId.toString(), null);
        }
    }
}
