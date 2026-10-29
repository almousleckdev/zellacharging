import { describe, expect, it } from "vitest";
import { presetRange, rangeToInstants } from "./dateRange";

describe("dateRange", () => {
  const now = new Date(2026, 9, 29, 15, 0, 0);

  it("builds the preset ranges ending today", () => {
    expect(presetRange("today", now)).toEqual({ from: "2026-10-29", to: "2026-10-29" });
    expect(presetRange("last7", now)).toEqual({ from: "2026-10-23", to: "2026-10-29" });
    expect(presetRange("last30", now)).toEqual({ from: "2026-09-30", to: "2026-10-29" });
    expect(presetRange("thisMonth", now)).toEqual({ from: "2026-10-01", to: "2026-10-29" });
  });

  it("turns whole days into a half-open instant range that includes the last day", () => {
    const { from, to } = rangeToInstants({ from: "2026-10-01", to: "2026-10-02" });

    expect(new Date(to as string).getTime() - new Date(from as string).getTime()).toBe(2 * 24 * 60 * 60 * 1000);
  });

  it("leaves open ends out", () => {
    expect(rangeToInstants({})).toEqual({});
  });
});
