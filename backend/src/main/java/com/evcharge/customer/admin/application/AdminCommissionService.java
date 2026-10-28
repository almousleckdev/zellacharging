package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.CommissionLog;
import com.evcharge.customer.charging.CommissionPage;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCommissionService {

    private static final int RECENT = 50;

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final CommissionLog commissions;

    public AdminCommissionService(AdminAccessService access, CountryCatalog catalog, CommissionLog commissions) {
        this.access = access;
        this.catalog = catalog;
        this.commissions = commissions;
    }

    public List<CountryCommission> list(AdminAccess admin, String country) {
        List<String> scope = access.scope(admin, Permission.PAYMENTS_READ, country);
        return catalog.all().stream().filter(item -> scope.contains(item.code())).map(item -> {
            CommissionPage page = TenantContext.callAs(item.code(), () -> commissions.search(RECENT));
            return new CountryCommission(item, page.totals(), page.items());
        }).toList();
    }
}
