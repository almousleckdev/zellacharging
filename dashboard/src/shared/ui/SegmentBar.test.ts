import { describe, expect, it } from "vitest";
import { cellsFor } from "./SegmentBar";

describe("cellsFor", () => {
  it("shares the cells in proportion and always uses all of them", () => {
    const counts = cellsFor(
      [
        { amount: 738, className: "a" },
        { amount: 7262, className: "b" },
        { amount: 0, className: "c" },
      ],
      40,
    );

    expect(counts).toEqual([4, 36, 0]);
    expect(counts.reduce((sum, count) => sum + count, 0)).toBe(40);
  });

  it("draws nothing when there is no money", () => {
    expect(cellsFor([{ amount: 0, className: "a" }], 40)).toEqual([0]);
  });

  it("gives a rounding remainder to the largest fractions only", () => {
    const counts = cellsFor(
      [
        { amount: 1, className: "a" },
        { amount: 1, className: "b" },
        { amount: 1, className: "c" },
      ],
      10,
    );

    expect(counts.reduce((sum, count) => sum + count, 0)).toBe(10);
    expect(Math.max(...counts) - Math.min(...counts)).toBe(1);
  });
});
