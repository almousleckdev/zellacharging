package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.ComplimentaryCategory;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.FreeUsage;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminFreeUsageService {

    private final AdminAccessService access;
    private final FreeUsage usage;

    public AdminFreeUsageService(AdminAccessService access, FreeUsage usage) {
        this.access = access;
        this.usage = usage;
    }

    public List<CountryFreeUsage> list(AdminAccess admin, String country, ComplimentaryCategory category,
                                       Instant from, Instant to) {
        ComplimentaryCategory kind = category == null ? ComplimentaryCategory.NDZELA_STAFF : category;
        List<CountryFreeUsage> items = new ArrayList<>();
        for (String code : access.scope(admin, Permission.COMPLIMENTARY_MANAGE, country)) {
            TenantContext.callAs(code, () -> usage.byCustomer(kind.name(), from, to))
                    .forEach(row -> items.add(new CountryFreeUsage(code, row)));
        }
        return items;
    }
}
