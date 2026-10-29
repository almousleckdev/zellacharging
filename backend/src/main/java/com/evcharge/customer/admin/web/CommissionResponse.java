package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.CommissionListing;
import com.evcharge.customer.admin.application.CountryCommissionTotals;
import com.evcharge.customer.admin.application.CountryEntry;
import com.evcharge.customer.charging.CommissionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

record CommissionResponse(List<Summary> summaries, long total, int page, int size, List<Item> items) {

    static CommissionResponse from(CommissionListing listing) {
        var found = listing.items();
        return new CommissionResponse(listing.summaries().stream().map(Summary::from).toList(), found.total(),
                found.page(), found.size(), found.items().stream().map(Item::from).toList());
    }

    record Summary(String country, String currency, int decimals, long sessions, long electricMinor,
                   long serviceFeeMinor, long vatMinor, BigDecimal serviceFeePercent, BigDecimal vatPercent) {

        static Summary from(CountryCommissionTotals entry) {
            var totals = entry.totals();
            return new Summary(entry.country().code(), entry.country().currency(), entry.country().currencyDecimals(),
                    totals.sessions(), totals.electricMinor(), totals.serviceFeeMinor(), totals.vatMinor(),
                    totals.serviceFeePercent(), totals.vatPercent());
        }
    }

    record Item(UUID sessionId, String country, String customerName, String stationId, long electricMinor,
                long serviceFeeMinor, long vatMinor, long totalMinor, String currency, int decimals, Instant at) {

        static Item from(CountryEntry<CommissionRecord> entry) {
            CommissionRecord record = entry.item();
            return new Item(record.sessionId(), entry.country(), record.customerName(), record.stationId(),
                    record.electricMinor(), record.serviceFeeMinor(), record.vatMinor(), record.totalMinor(),
                    record.currency(), record.decimals(), record.at());
        }
    }
}
