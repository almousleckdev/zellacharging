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
    sessions: { live: 1, completed: 5, startedLast24Hours: 3, energyKwh: 40.5, revenueMinor: 6000 },
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
  it("adds counts across countries", () => {
    const totals = overviewTotals([country("GA", "XAF"), country("ML", "XOF")]);

    expect(totals.customers).toEqual({ total: 20, active: 16, recent: 4 });
    expect(totals.sessions).toEqual({ live: 2, startedLast24Hours: 6, completed: 10, energyKwh: 81 });
    expect(totals.pendingTopups).toBe(2);
    expect(totals.pendingWithdrawals).toBe(4);
  });

  it("never adds money in different currencies together", () => {
    const totals = overviewTotals([country("GA", "XAF"), country("ML", "XOF")]);

    expect(totals.revenue).toEqual([
      { currency: "XAF", decimals: 0, minor: 6000 },
      { currency: "XOF", decimals: 0, minor: 6000 },
    ]);
  });

  it("adds money that shares a currency", () => {
    const totals = overviewTotals([country("GA", "XAF"), country("CM", "XAF")]);

    expect(totals.walletBalance).toEqual([{ currency: "XAF", decimals: 0, minor: 10000 }]);
    expect(totals.reserved).toEqual([{ currency: "XAF", decimals: 0, minor: 1000 }]);
  });

  it("returns zeros and no money lines when there is no country", () => {
    const totals = overviewTotals([]);

    expect(totals.customers.total).toBe(0);
    expect(totals.revenue).toEqual([]);
  });
});
