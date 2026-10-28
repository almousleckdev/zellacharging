import type { MoneyAmount } from "@/shared/lib/format";
import type { CountryFigures } from "./schemas";

export type OverviewTotals = {
  customers: { total: number; active: number; recent: number };
  sessions: { live: number; startedLast24Hours: number; completed: number; energyKwh: number };
  pendingTopups: number;
  pendingWithdrawals: number;
  revenue: MoneyAmount[];
  walletBalance: MoneyAmount[];
  reserved: MoneyAmount[];
  topups: MoneyAmount[];
  withdrawals: MoneyAmount[];
  captured: MoneyAmount[];
};

function sumByCurrency(countries: CountryFigures[], pick: (country: CountryFigures) => number): MoneyAmount[] {
  const totals = new Map<string, MoneyAmount>();
  for (const country of countries) {
    const known = totals.get(country.currency);
    const minor = (known?.minor ?? 0) + pick(country);
    totals.set(country.currency, { currency: country.currency, decimals: country.currencyDecimals, minor });
  }
  return [...totals.values()];
}

function sum(countries: CountryFigures[], pick: (country: CountryFigures) => number): number {
  return countries.reduce((total, country) => total + pick(country), 0);
}

export function overviewTotals(countries: CountryFigures[]): OverviewTotals {
  return {
    customers: {
      total: sum(countries, (c) => c.customers.total),
      active: sum(countries, (c) => c.customers.active),
      recent: sum(countries, (c) => c.customers.newLast30Days),
    },
    sessions: {
      live: sum(countries, (c) => c.sessions.live),
      startedLast24Hours: sum(countries, (c) => c.sessions.startedLast24Hours),
      completed: sum(countries, (c) => c.sessions.completed),
      energyKwh: sum(countries, (c) => c.sessions.energyKwh),
    },
    pendingTopups: sum(countries, (c) => c.wallets.pendingTopups),
    pendingWithdrawals: sum(countries, (c) => c.wallets.pendingWithdrawals),
    revenue: sumByCurrency(countries, (c) => c.sessions.revenueMinor),
    walletBalance: sumByCurrency(countries, (c) => c.wallets.balanceMinor),
    reserved: sumByCurrency(countries, (c) => c.wallets.reservedMinor),
    topups: sumByCurrency(countries, (c) => c.wallets.topupsSucceededMinor),
    withdrawals: sumByCurrency(countries, (c) => c.wallets.withdrawalsPaidMinor),
    captured: sumByCurrency(countries, (c) => c.wallets.capturedMinor),
  };
}
