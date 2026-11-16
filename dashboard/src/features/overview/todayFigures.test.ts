import { describe, expect, it } from "vitest";
import type { CurrencyPeriod } from "./seriesMath";
import type { SeriesPoint } from "./seriesSchema";
import { todayFigures } from "./todayFigures";

function point(day: string, capturedMinor: number): SeriesPoint {
  return {
    day,
    topupsMinor: 0,
    topupCount: 0,
    withdrawalsMinor: 0,
    capturedMinor,
    sessions: 0,
    completed: 0,
    failed: 0,
    energyKwh: 0,
    revenueMinor: 0,
    serviceFeeMinor: 0,
    vatMinor: 0,
  };
}

function period(amounts: number[]): CurrencyPeriod {
  const daily = amounts.map((amount, index) => point(`2026-11-${String(index + 1).padStart(2, "0")}`, amount));
  const empty = point("", 0);
  return { currency: "XAF", decimals: 0, daily, current: empty, previous: empty };
}

describe("todayFigures", () => {
  it("takes the last day as today, the one before as yesterday, and the last seven days for the trend", () => {
    const figures = todayFigures(period([1, 2, 3, 4, 5, 6, 7, 8, 9]));

    expect(figures.today.capturedMinor).toBe(9);
    expect(figures.yesterday?.capturedMinor).toBe(8);
    expect(figures.week.map((day) => day.capturedMinor)).toEqual([3, 4, 5, 6, 7, 8, 9]);
  });
});
