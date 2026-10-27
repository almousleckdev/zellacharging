import type { Dictionary } from "../types";

export const enOverview: Pick<Dictionary, "overview"> = {
  overview: {
    title: "Overview",
    subtitle: "Consolidated platform indicators.",
    viewing: "Scope shown",
    loadError: "We couldn't load the indicators.",
    metrics: {
      customers: "Customers",
      liveSessions: "Live sessions",
      energy: "Energy delivered",
      revenue: "Revenue",
      walletBalance: "Wallet balances",
      topups: "Top-ups collected",
      withdrawals: "Withdrawals paid",
    },
    hints: {
      customers: "{active} active · {recent} new in 30 days",
      liveSessions: "{count} started in 24 h",
      energy: "{count} completed sessions",
      revenue: "Completed charging sessions",
      walletBalance: "of which {reserved} reserved",
      topups: "{count} pending",
      withdrawals: "{count} pending",
    },
    table: {
      title: "Breakdown by country",
      country: "Country",
      customers: "Customers",
      liveSessions: "Live sessions",
      energy: "Energy",
      revenue: "Revenue",
      balance: "Balances",
    },
    unitKwh: "kWh",
  },
};
