import { describe, expect, it } from "vitest";
import type { CountryFigures } from "./schemas";
import { overviewTotals } from "./totals";

function country(code: string, currency: string, overrides: Partial<CountryFigures> = {}): CountryFigures {
  return {
    code,
    name: code,
    currency,
    currencyDecimals: 0,
    customers: { total: 10, active: 8, newLast30Days: 2 },
    sessions: { live: 1, completed: 5, startedLast24Hours: 3, energyKwh: 40.5, revenueMinor: 6000, complimentaryKwh: 0, complimentaryValueMinor: 0 },
    wallets: {
      count: 9,
      balanceMinor: 5000,
      reservedMinor: 500,
      topupsSucceededMinor: 9000,
      pendingTopups: 1,
      withdrawalsPaidMinor: 2000,
      pendingWithdrawals: 2,
      capturedMinor: 1500,
    },
    ...overrides,
  };
}

describe("overviewTotals", () => {
  it("adds the customer and live-charge counts across countries and carries no wallet money", () => {
    const totals = overviewTotals([country("GA", "XAF"), country("ML", "XOF")]);

    expect(totals).toEqual({ customers: { total: 20, active: 16 }, sessions: { live: 2 } });
  });
});
