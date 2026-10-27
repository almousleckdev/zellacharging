package com.evcharge.customer.admin.application;

import com.evcharge.customer.account.CustomerStatistics;
import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.SessionStatistics;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.WalletStatistics;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminOverviewService {

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final CustomerStatistics customers;
    private final SessionStatistics sessions;
    private final WalletStatistics wallets;

    public AdminOverviewService(AdminAccessService access, CountryCatalog catalog, CustomerStatistics customers,
                                SessionStatistics sessions, WalletStatistics wallets) {
        this.access = access;
        this.catalog = catalog;
        this.customers = customers;
        this.sessions = sessions;
        this.wallets = wallets;
    }

    public AdminOverview overview(AdminAccess admin, String requested) {
        List<String> scope = access.scope(admin, Permission.OVERVIEW_READ, requested);
        List<CountryOverview> countries = catalog.all().stream()
                .filter(country -> scope.contains(country.code()))
                .map(this::overviewOf)
                .toList();
        return new AdminOverview(scope.size() == 1 ? scope.getFirst() : AdminAccessService.ALL_COUNTRIES, countries);
    }

    private CountryOverview overviewOf(Country country) {
        return TenantContext.callAs(country.code(), () -> new CountryOverview(country, customers.figures(),
                sessions.figures(), wallets.figures()));
    }
}
