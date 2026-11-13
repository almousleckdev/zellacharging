package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminOverview;
import com.evcharge.customer.admin.application.CountryOverview;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record AdminOverviewResponse(String scope, Instant since, List<CountryFigures> countries) {

    static AdminOverviewResponse from(AdminOverview overview, Instant since) {
        return new AdminOverviewResponse(overview.scope(), since, overview.countries().stream().map(CountryFigures::from).toList());
    }

    record CountryFigures(String code, String name, String currency, int currencyDecimals, String phoneRegion,
                          Customers customers, Sessions sessions, Wallets wallets) {

        static CountryFigures from(CountryOverview overview) {
            var country = overview.country();
            var customers = overview.customers();
            var sessions = overview.sessions();
            var wallets = overview.wallets();
            return new CountryFigures(country.code(), country.name(), country.currency(), country.currencyDecimals(),
                    country.phoneRegion(), new Customers(customers.total(), customers.active(), customers.newLast30Days()),
                    new Sessions(sessions.live(), sessions.completed(), sessions.startedLast24Hours(),
                            sessions.energyKwh(), sessions.revenueMinor(), sessions.complimentaryKwh(),
                            sessions.complimentaryValueMinor()),
                    new Wallets(wallets.wallets(), wallets.balanceMinor(), wallets.reservedMinor(),
                            wallets.topupsSucceededMinor(), wallets.pendingTopups(), wallets.withdrawalsPaidMinor(),
                            wallets.pendingWithdrawals(), wallets.capturedMinor()));
        }
    }

    record Customers(long total, long active, long newLast30Days) {
    }

    record Sessions(long live, long completed, long startedLast24Hours, BigDecimal energyKwh, long revenueMinor,
                    BigDecimal complimentaryKwh, long complimentaryValueMinor) {
    }

    record Wallets(long count, long balanceMinor, long reservedMinor, long topupsSucceededMinor, long pendingTopups,
                   long withdrawalsPaidMinor, long pendingWithdrawals, long capturedMinor) {
    }
}
