package com.evcharge.customer.admin.application;

import com.evcharge.customer.admin.application.port.CountryCatalog;
import com.evcharge.customer.admin.domain.AdminAccess;
import com.evcharge.customer.admin.domain.Permission;
import com.evcharge.customer.charging.ActivityFlows;
import com.evcharge.customer.charging.DailyActivity;
import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Bucket;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.TenantContext;
import com.evcharge.customer.wallet.DailyMoney;
import com.evcharge.customer.wallet.MoneyFlows;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AdminSeriesService {

    private static final Duration MAX_RANGE = Duration.ofDays(800);

    private final AdminAccessService access;
    private final CountryCatalog catalog;
    private final MoneyFlows money;
    private final ActivityFlows activity;

    public AdminSeriesService(AdminAccessService access, CountryCatalog catalog, MoneyFlows money,
                              ActivityFlows activity) {
        this.access = access;
        this.catalog = catalog;
        this.money = money;
        this.activity = activity;
    }

    public List<CountrySeries> series(AdminAccess admin, String country, Instant from, Instant to, String bucket) {
        List<String> scope = access.scope(admin, Permission.OVERVIEW_READ, country);
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(Duration.ofDays(30)) : from;
        if (!start.isBefore(end) || Duration.between(start, end).compareTo(MAX_RANGE) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RANGE", "Invalid date range");
        }
        Bucket grain = parse(bucket);
        return catalog.all().stream().filter(item -> scope.contains(item.code()))
                .map(item -> TenantContext.callAs(item.code(), () -> merged(item, start, end, grain))).toList();
    }

    private CountrySeries merged(Country country, Instant from, Instant to, Bucket bucket) {
        Map<LocalDate, SeriesPoint> points = new TreeMap<>();
        for (DailyMoney flow : money.between(from, to, bucket)) {
            points.put(flow.day(), new SeriesPoint(flow.day(), flow.topupsMinor(), flow.topupCount(),
                    flow.withdrawalsMinor(), flow.capturedMinor(), 0, 0, 0, BigDecimal.ZERO, 0, 0, 0));
        }
        for (DailyActivity day : activity.between(from, to, bucket)) {
            SeriesPoint base = points.getOrDefault(day.day(), new SeriesPoint(day.day(), 0, 0, 0, 0, 0, 0, 0,
                    BigDecimal.ZERO, 0, 0, 0));
            points.put(day.day(), new SeriesPoint(day.day(), base.topupsMinor(), base.topupCount(),
                    base.withdrawalsMinor(), base.capturedMinor(), day.sessions(), day.completed(), day.failed(),
                    day.energyKwh(), day.revenueMinor(), day.serviceFeeMinor(), day.vatMinor()));
        }
        return new CountrySeries(country, LocalDate.now(ZoneId.of(country.timeZone())), List.copyOf(points.values()));
    }

    private static Bucket parse(String value) {
        try {
            return Bucket.parse(value);
        } catch (IllegalArgumentException invalid) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_BUCKET", "Invalid bucket");
        }
    }
}
