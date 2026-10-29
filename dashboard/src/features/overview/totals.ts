import type { CountryFigures } from "./schemas";

export type OverviewTotals = {
  customers: { total: number; active: number };
  sessions: { live: number };
};

export function overviewTotals(countries: CountryFigures[]): OverviewTotals {
  return {
    customers: {
      total: countries.reduce((sum, country) => sum + country.customers.total, 0),
      active: countries.reduce((sum, country) => sum + country.customers.active, 0),
    },
    sessions: { live: countries.reduce((sum, country) => sum + country.sessions.live, 0) },
  };
}
