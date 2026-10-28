package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CountryCommission;
import com.evcharge.customer.charging.CommissionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CommissionResponse(List<Summary> summaries, List<Item> items) {

    static CommissionResponse from(List<CountryCommission> countries) {
        List<Summary> summaries = countries.stream().map(Summary::from).toList();
        List<Item> items = countries.stream()
                .flatMap(country -> country.items().stream().map(record -> Item.from(country, record)))
                .sorted((a, b) -> b.at().compareTo(a.at())).toList();
        return new CommissionResponse(summaries, items);
    }

    record Summary(String country, String currency, int decimals, long sessions, long electricMinor,
                   long serviceFeeMinor, long vatMinor, BigDecimal serviceFeePercent, BigDecimal vatPercent) {

        static Summary from(CountryCommission country) {
            var totals = country.totals();
            return new Summary(country.country().code(), country.country().currency(),
                    country.country().currencyDecimals(), totals.sessions(), totals.electricMinor(),
                    totals.serviceFeeMinor(), totals.vatMinor(), totals.serviceFeePercent(), totals.vatPercent());
        }
    }

    record Item(UUID sessionId, String country, String customerName, String stationId, long electricMinor,
                long serviceFeeMinor, long vatMinor, long totalMinor, String currency, int decimals, Instant at) {

        static Item from(CountryCommission country, CommissionRecord record) {
            String currency = record.currency() == null ? country.country().currency() : record.currency();
            return new Item(record.sessionId(), country.country().code(), record.customerName(),
                    record.stationId(), record.electricMinor(), record.serviceFeeMinor(), record.vatMinor(),
                    record.totalMinor(), currency, country.country().currencyDecimals(), record.at());
        }
    }
}
