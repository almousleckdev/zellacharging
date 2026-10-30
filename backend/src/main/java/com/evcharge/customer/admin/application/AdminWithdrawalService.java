package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WithdrawalReviews;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminWithdrawalService {

    private final AdminAccessService access;
    private final WithdrawalReviews reviews;
    private final AdminAudit audit;

    public AdminWithdrawalService(AdminAccessService access, WithdrawalReviews reviews, AdminAudit audit) {
        this.access = access;
        this.reviews = reviews;
        this.audit = audit;
    }

    public void approve(AdminAccess admin, String country, UUID id, String note) {
        String code = countryOf(admin, country);
        TenantContext.runAs(code, () -> reviews.approve(admin.id(), id, clean(note)));
        audit.record(admin.id(), code, "WITHDRAWAL_APPROVED", id.toString(), clean(note));
    }

    public void reject(AdminAccess admin, String country, UUID id, String note) {
        String code = countryOf(admin, country);
        TenantContext.runAs(code, () -> reviews.reject(admin.id(), id, clean(note)));
        audit.record(admin.id(), code, "WITHDRAWAL_REJECTED", id.toString(), clean(note));
    }

    private String countryOf(AdminAccess admin, String country) {
        return AdminCountries.single(access.scope(admin, Permission.WITHDRAWALS_REVIEW, country));
    }

    private static String clean(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }
}
