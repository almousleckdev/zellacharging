package com.evcharge.customer.admin.web;

import com.evcharge.customer.admin.application.AdminOverview;
import com.evcharge.customer.admin.application.CountryOverview;

import java.math.BigDecimal;
import java.util.List;

record AdminOverviewResponse(String scope, List<CountryFigures> countries) {

    static AdminOverviewResponse from(AdminOverview overview) {
        return new AdminOverviewResponse(overview.scope(), overview.countries().stream().map(CountryFigures::from).toList());
    }

    record CountryFigures(String code, String name, String currency, int currencyDecimals, Customers customers,
                          Sessions sessions, Wallets wallets) {

        static CountryFigures from(CountryOverview overview) {
            var country = overview.country();
            var customers = overview.customers();
            var sessions = overview.sessions();
            var wallets = overview.wallets();
            return new CountryFigures(country.code(), country.name(), country.currency(), country.currencyDecimals(),
                    new Customers(customers.total(), customers.active(), customers.newLast30Days()),
                    new Sessions(sessions.live(), sessions.completed(), sessions.startedLast24Hours(),
                            sessions.energyKwh(), sessions.revenueMinor()),
                    new Wallets(wallets.wallets(), wallets.balanceMinor(), wallets.reservedMinor(),
                            wallets.topupsSucceededMinor(), wallets.pendingTopups(), wallets.withdrawalsPaidMinor(),
                            wallets.pendingWithdrawals()));
        }
    }

    record Customers(long total, long active, long newLast30Days) {
    }

    record Sessions(long live, long completed, long startedLast24Hours, BigDecimal energyKwh, long revenueMinor) {
    }

    record Wallets(long count, long balanceMinor, long reservedMinor, long topupsSucceededMinor, long pendingTopups,
                   long withdrawalsPaidMinor, long pendingWithdrawals) {
    }
}
