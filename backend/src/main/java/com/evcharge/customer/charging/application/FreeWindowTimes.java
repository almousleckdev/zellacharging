package com.evcharge.customer.charging.application;

import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import com.evcharge.customer.shared.TenantContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class FreeWindowTimes {

    private static final DateTimeFormatter HOURS_AND_MINUTES = DateTimeFormatter.ofPattern("HH:mm");
    private static final String LAST_MINUTE = "23:59";

    private final CountryDirectory countries;

    public FreeWindowTimes(CountryDirectory countries) {
        this.countries = countries;
    }

    public String display(Instant endsAt) {
        LocalTime time = endsAt.atZone(zone()).toLocalTime();
        return LocalTime.MIDNIGHT.equals(time) ? LAST_MINUTE : time.format(HOURS_AND_MINUTES);
    }

    private ZoneId zone() {
        return TenantContext.current().flatMap(countries::activeByCode).map(Country::timeZone).map(ZoneId::of)
                .orElse(ZoneOffset.UTC);
    }
}
