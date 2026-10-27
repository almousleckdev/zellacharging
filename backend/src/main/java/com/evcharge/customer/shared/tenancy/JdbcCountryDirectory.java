package com.evcharge.customer.shared.tenancy;

import com.evcharge.customer.shared.ApiException;
import com.evcharge.customer.shared.Country;
import com.evcharge.customer.shared.CountryDirectory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class JdbcCountryDirectory implements CountryDirectory {

    private final JdbcClient jdbc;
    private final TenancySettings settings;
    private volatile List<Country> cached = List.of();
    private volatile long loadedAt;

    JdbcCountryDirectory(JdbcClient jdbc, TenancySettings settings) {
        this.jdbc = jdbc;
        this.settings = settings;
    }

    @Override
    public List<Country> active() {
        long now = System.nanoTime();
        if (cached.isEmpty() || now - loadedAt > settings.countryCacheTtl().toNanos()) {
            cached = jdbc.sql("select code, name, currency, currency_decimals, phone_region from country "
                            + "where active order by code")
                    .query((rs, rowNum) -> new Country(rs.getString("code"), rs.getString("name"),
                            rs.getString("currency"), rs.getInt("currency_decimals"), rs.getString("phone_region")))
                    .list();
            loadedAt = now;
        }
        return cached;
    }

    @Override
    public Optional<Country> activeByCode(String code) {
        return active().stream().filter(country -> country.code().equals(code)).findFirst();
    }

    @Override
    public Optional<Country> activeByPhoneRegion(String phoneRegion) {
        return active().stream().filter(country -> country.phoneRegion().equals(phoneRegion)).findFirst();
    }

    @Override
    public Country defaultCountry() {
        return activeByCode(settings.defaultCountry()).orElseThrow(() -> new ApiException(
                HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "No active default country"));
    }
}
