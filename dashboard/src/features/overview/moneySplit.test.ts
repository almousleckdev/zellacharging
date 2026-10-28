import { describe, expect, it } from "vitest";
import { activeSplits, moneySplits } from "./moneySplit";
import type { CountryFigures } from "./schemas";

function country(currency: string, wallets: Partial<CountryFigures["wallets"]>): CountryFigures {
  return {
    code: "GA",
    name: "Gabon",
    currency,
    currencyDecimals: 0,
    customers: { total: 1, active: 1, newLast30Days: 0 },
    sessions: { live: 0, completed: 0, startedLast24Hours: 0, energyKwh: 0, revenueMinor: 0 },
    wallets: {
      count: 1,
      balanceMinor: 0,
      reservedMinor: 0,
      topupsSucceededMinor: 0,
      pendingTopups: 0,
      withdrawalsPaidMinor: 0,
      pendingWithdrawals: 0,
      capturedMinor: 0,
      ...wallets,
    },
  };
}

describe("moneySplits", () => {
  it("splits what is held between customers and Zella", () => {
    const [split] = moneySplits([
      country("XAF", { topupsSucceededMinor: 2500, balanceMinor: 1862, capturedMinor: 138 }),
    ]);

    expect(split).toMatchObject({ heldMinor: 2500, owedMinor: 1862, earnedMinor: 138, inFlightMinor: 500 });
  });

  it("keeps currencies apart", () => {
    expect(moneySplits([country("XAF", {}), country("XOF", {})])).toHaveLength(2);
  });

  it("never reports negative money in flight", () => {
    const [split] = moneySplits([country("XAF", { topupsSucceededMinor: 100, balanceMinor: 300 })]);

    expect(split.inFlightMinor).toBe(0);
  });

  it("hides a currency with no money unless nothing else exists", () => {
    const active = country("XAF", { topupsSucceededMinor: 100, balanceMinor: 100 });

    expect(activeSplits([active, country("XOF", {})]).map((split) => split.currency)).toEqual(["XAF"]);
    expect(activeSplits([country("XOF", {})]).map((split) => split.currency)).toEqual(["XOF"]);
  });
});
