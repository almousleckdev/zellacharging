package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.catalog.StationInventories;
import com.evcharge.customer.catalog.StationInventory;
import com.evcharge.customer.charging.StationActivity;
import com.evcharge.customer.charging.StationStatistics;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminStationService {

    private final AdminAccessService access;
    private final StationInventories inventories;
    private final StationStatistics statistics;

    public AdminStationService(AdminAccessService access, StationInventories inventories,
                               StationStatistics statistics) {
        this.access = access;
        this.inventories = inventories;
        this.statistics = statistics;
    }

    public List<StationView> list(AdminAccess admin, String country) {
        List<String> scope = access.scope(admin, Permission.STATIONS_READ, country);
        Map<String, StationActivity> activity = activityOf(scope);
        return inventories.all().stream().filter(station -> inScope(scope, station))
                .map(station -> view(station, activity)).toList();
    }

    public StationView detail(AdminAccess admin, String stationId) {
        access.require(admin, Permission.STATIONS_READ);
        StationInventory station = inventories.find(stationId).filter(found -> admin.canSee(found.isoCode()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "Station not found"));
        return view(station, activityOf(List.of(station.isoCode().toUpperCase())));
    }

    private Map<String, StationActivity> activityOf(List<String> countries) {
        Map<String, StationActivity> merged = new HashMap<>();
        countries.forEach(code -> merged.putAll(TenantContext.callAs(code, statistics::activity)));
        return merged;
    }

    private static boolean inScope(List<String> scope, StationInventory station) {
        return station.isoCode() != null && scope.contains(station.isoCode().toUpperCase());
    }

    private static StationView view(StationInventory station, Map<String, StationActivity> activity) {
        return new StationView(station, activity.getOrDefault(station.id(),
                new StationActivity(station.id(), 0, 0, BigDecimal.ZERO, 0, null)));
    }
}
