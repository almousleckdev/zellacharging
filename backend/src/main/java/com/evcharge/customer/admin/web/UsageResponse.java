package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryUsage;
import com.evcharge.customer.charging.UsageSummary;
import com.evcharge.customer.charging.UsageTotals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

record UsageResponse(List<Country> countries) {

    static UsageResponse from(List<CountryUsage> usage) {
        return new UsageResponse(usage.stream().map(Country::from).toList());
    }

    record Country(String code, String name, String currency, int decimals, LocalDate day, Totals today,
                   Totals yesterday, Totals week, Totals month) {

        static Country from(CountryUsage usage) {
            var country = usage.country();
            UsageSummary summary = usage.summary();
            return new Country(country.code(), country.name(), country.currency(), country.currencyDecimals(),
                    summary.day(), Totals.from(summary.today()), Totals.from(summary.yesterday()),
                    Totals.from(summary.week()), Totals.from(summary.month()));
        }
    }

    record Totals(long sessions, BigDecimal energyKwh, long collectedMinor, BigDecimal staffEnergyKwh) {

        static Totals from(UsageTotals totals) {
            return new Totals(totals.sessions(), totals.energyKwh(), totals.collectedMinor(),
                    totals.staffEnergyKwh());
        }
    }
}
