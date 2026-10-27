import { describe, expect, it } from "vitest";
import { interpolate } from "../i18n/interpolate";
import { formatMoney, formatMoneyLines, formatNumber } from "./format";

const normalize = (text: string) => text.replace(/[  ]/g, " ");

describe("formatMoney", () => {
  it("shows a zero-decimal currency without fractions", () => {
    expect(normalize(formatMoney({ currency: "XAF", decimals: 0, minor: 12500 }, "fr-FR"))).toBe("12 500 FCFA");
  });

  it("scales minor units by the currency decimals", () => {
    expect(formatMoney({ currency: "EUR", decimals: 2, minor: 12345 }, "en-US")).toBe("€123.45");
  });
});

describe("formatMoneyLines", () => {
  it("gives one line per currency", () => {
    const lines = formatMoneyLines(
      [
        { currency: "XAF", decimals: 0, minor: 1000 },
        { currency: "XOF", decimals: 0, minor: 2000 },
      ],
      "fr-FR",
      "XAF",
    );

    expect(lines.map(normalize)).toEqual(["1 000 XAF", "2 000 XOF"]);
  });

  it("keeps the symbol when there is a single currency", () => {
    expect(formatMoneyLines([{ currency: "XAF", decimals: 0, minor: 1000 }], "fr-FR", "XAF").map(normalize)).toEqual([
      "1 000 FCFA",
    ]);
  });

  it("shows a zero in the fallback currency when there is nothing", () => {
    expect(normalize(formatMoneyLines([], "fr-FR", "XAF")[0])).toBe("0 FCFA");
  });
});

describe("formatNumber", () => {
  it("groups digits and keeps the requested decimals", () => {
    expect(normalize(formatNumber(1234.5, "fr-FR", 1))).toBe("1 234,5");
  });
});

describe("interpolate", () => {
  it("fills named placeholders and leaves unknown ones visible", () => {
    expect(interpolate("{count} en attente, {other}", { count: 3 })).toBe("3 en attente, {other}");
  });
});
