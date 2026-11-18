import { describe, expect, it } from "vitest";
import { periodStart } from "./periods";

const NOW = new Date("2026-11-18T15:30:00Z");

describe("periodStart", () => {
  it("starts today at midnight", () => {
    expect(periodStart("today", NOW)).toBe("2026-11-18T00:00:00.000Z");
  });

  it("covers the last seven days including today", () => {
    expect(periodStart("week", NOW)).toBe("2026-11-12T00:00:00.000Z");
  });

  it("covers the last thirty days including today", () => {
    expect(periodStart("month", NOW)).toBe("2026-10-20T00:00:00.000Z");
  });

  it("has no start when everything is asked", () => {
    expect(periodStart("all", NOW)).toBeNull();
  });
});
