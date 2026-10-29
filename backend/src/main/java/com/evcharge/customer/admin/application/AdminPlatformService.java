package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.StationPrices;
import com.evcharge.customer.shared.HealthProbe;
import com.evcharge.customer.shared.SettingsSource;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Service
public class AdminPlatformService {

    private static final Set<String> PRICING = Set.of("charging", "wallet");

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final List<SettingsSource> sources;
    private final List<HealthProbe> probes;
    private final StationPrices prices;

    public AdminPlatformService(AdminAccessService access, CountryCatalog catalog, List<SettingsSource> sources,
                                List<HealthProbe> probes, StationPrices prices) {
        this.access = access;
        this.catalog = catalog;
        this.sources = sources;
        this.probes = probes;
        this.prices = prices;
    }

    public List<PlatformSettings> pricing(AdminAccess admin) {
        access.require(admin, Permission.OVERVIEW_READ);
        return settings(PRICING::contains);
    }

    public List<CountryStationPrices> stationPrices(AdminAccess admin, String country) {
        List<String> scope = access.scope(admin, Permission.OVERVIEW_READ, country);
        return catalog.all().stream().filter(item -> scope.contains(item.code()))
                .map(item -> new CountryStationPrices(item, TenantContext.callAs(item.code(), prices::applied)))
                .toList();
    }

    public List<PlatformSettings> system(AdminAccess admin) {
        access.require(admin, Permission.ADMINS_MANAGE);
        return settings(section -> true);
    }

    public List<ProbeReport> probes(AdminAccess admin) {
        access.require(admin, Permission.ADMINS_MANAGE);
        return probes.stream().map(probe -> new ProbeReport(probe.name(), probe.check())).toList();
    }

    private List<PlatformSettings> settings(Predicate<String> wanted) {
        return sources.stream().filter(source -> wanted.test(source.section()))
                .map(source -> new PlatformSettings(source.section(), source.values())).toList();
    }
}
