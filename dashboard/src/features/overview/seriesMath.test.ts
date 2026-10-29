import { describe, expect, it } from "vitest";
import { deltaPercent, periodsByCurrency } from "./seriesMath";
import type { SeriesResponse } from "./seriesSchema";

const base = { topupCount: 0, withdrawalsMinor: 0, sessions: 0, completed: 0, failed: 0, energyKwh: 0, revenueMinor: 0, serviceFeeMinor: 0, vatMinor: 0 };

describe("periodsByCurrency", () => {
  const now = new Date("2026-10-29T10:00:00Z");

  it("fills missing days, sums the current period and the one before, and groups by currency", () => {
    const response: SeriesResponse = {
      countries: [
        { code: "GA", name: "Gabon", currency: "XAF", decimals: 0, points: [
          { day: "2026-10-29", topupsMinor: 100, capturedMinor: 40, ...base },
          { day: "2026-10-27", topupsMinor: 50, capturedMinor: 10, ...base },
        ] },
        { code: "ML", name: "Mali", currency: "XOF", decimals: 0, points: [{ day: "2026-10-29", topupsMinor: 7, capturedMinor: 1, ...base }] },
      ],
    };

    const [xaf, xof] = periodsByCurrency(response, 2, now);

    expect(xaf.daily.map((day) => day.day)).toEqual(["2026-10-28", "2026-10-29"]);
    expect(xaf.current.topupsMinor).toBe(100);
    expect(xaf.previous.topupsMinor).toBe(50);
    expect(xof.currency).toBe("XOF");
    expect(xof.current.capturedMinor).toBe(1);
  });
});

describe("deltaPercent", () => {
  it("compares against the previous period", () => {
    expect(deltaPercent(150, 100)).toBe(50);
    expect(deltaPercent(50, 100)).toBe(-50);
    expect(deltaPercent(0, 0)).toBe(0);
    expect(deltaPercent(5, 0)).toBeNull();
  });
});
