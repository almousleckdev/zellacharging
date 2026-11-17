import { describe, expect, it } from "vitest";
import type { SeriesPoint } from "@/features/overview/seriesSchema";
import { collectionFigures } from "./collectionFigures";

function point(day: string, capturedMinor: number, sessions = 1): SeriesPoint {
  return {
    day,
    topupsMinor: 0,
    topupCount: 0,
    withdrawalsMinor: 0,
    capturedMinor,
    sessions,
    completed: sessions,
    failed: 0,
    energyKwh: 0,
    revenueMinor: 0,
    serviceFeeMinor: 0,
    vatMinor: 0,
  };
}

describe("collectionFigures", () => {
  const now = new Date("2026-11-18T10:00:00Z");
  const daily = [
    point("2026-10-31", 900),
    point("2026-11-01", 100),
    point("2026-11-15", 200),
    point("2026-11-16", 300),
    point("2026-11-17", 400),
    point("2026-11-18", 500),
  ];

  it("reads today and yesterday from the last two days", () => {
    const figures = collectionFigures(daily, now);

    expect(figures.today.capturedMinor).toBe(500);
    expect(figures.yesterdayMinor).toBe(400);
  });

  it("adds the days since Monday for the week", () => {
    expect(collectionFigures(daily, now).weekMinor).toBe(300 + 400 + 500);
  });

  it("adds the days since the first of the month for the month", () => {
    expect(collectionFigures(daily, now).monthMinor).toBe(100 + 200 + 300 + 400 + 500);
  });
});
