package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.UsageFigures;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminUsageService {

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final UsageFigures usage;

    public AdminUsageService(AdminAccessService access, CountryCatalog catalog, UsageFigures usage) {
        this.access = access;
        this.catalog = catalog;
        this.usage = usage;
    }

    public List<CountryUsage> summary(AdminAccess admin, String country, String station, UUID customerId) {
        List<String> scope = access.scope(admin, Permission.SESSIONS_READ, country);
        String stationId = station == null || station.isBlank() ? null : station.trim();
        return catalog.all().stream().filter(item -> scope.contains(item.code()))
                .map(item -> new CountryUsage(item,
                        TenantContext.callAs(item.code(), () -> usage.summary(stationId, customerId)))).toList();
    }
}
