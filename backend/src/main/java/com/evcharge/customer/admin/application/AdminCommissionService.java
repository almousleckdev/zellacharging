package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.CommissionLog;
import com.evcharge.customer.charging.CommissionPage;
import com.evcharge.customer.charging.CommissionQuery;
import com.evcharge.customer.charging.CommissionDigest;
import com.evcharge.customer.shared.LogFilter;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCommissionService {

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final CommissionLog commissions;

    public AdminCommissionService(AdminAccessService access, CountryCatalog catalog, CommissionLog commissions) {
        this.access = access;
        this.catalog = catalog;
        this.commissions = commissions;
    }

    public CommissionListing list(AdminAccess admin, String country, String text, LogFilter filter, int page,
                                  int size) {
        List<String> scope = access.scope(admin, Permission.PAYMENTS_READ, country);
        PageWindow window = PageWindow.of(page, size);
        String pattern = text == null || text.isBlank() ? null : text.trim();
        List<CountryCommissionTotals> summaries = catalog.all().stream().filter(item -> scope.contains(item.code()))
                .map(item -> new CountryCommissionTotals(item, TenantContext.callAs(item.code(),
                        () -> commissions.search(new CommissionQuery(pattern, filter, 1)).totals()))).toList();
        CountryListing<CommissionDigest> items = CountryMerge.newestFirst(scope, window, limit -> {
                    CommissionPage found = commissions.search(new CommissionQuery(pattern, filter, limit));
                    return new CountryMerge.Found<>(found.customers(), found.items());
                }, CommissionDigest::lastAt);
        return new CommissionListing(summaries, items);
    }
}
