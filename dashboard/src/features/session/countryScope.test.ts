import { describe, expect, it } from "vitest";
import { ALL_COUNTRIES, countryLabel, resolveCountry } from "./countryScope";
import type { Country } from "./schemas";

const gabon: Country = { code: "GA", name: "Gabon", currency: "XAF", currencyDecimals: 0 };
const mali: Country = { code: "ML", name: "Mali", currency: "XOF", currencyDecimals: 0 };

describe("resolveCountry", () => {
  it("defaults to every country for an admin who has several", () => {
    expect(resolveCountry(null, [gabon, mali])).toBe(ALL_COUNTRIES);
  });

  it("honours a requested country the admin may see", () => {
    expect(resolveCountry("ML", [gabon, mali])).toBe("ML");
    expect(resolveCountry(ALL_COUNTRIES, [gabon, mali])).toBe(ALL_COUNTRIES);
  });

  it("falls back when the URL asks for a country the admin cannot see", () => {
    expect(resolveCountry("ML", [gabon])).toBe("GA");
    expect(resolveCountry("XX", [gabon, mali])).toBe(ALL_COUNTRIES);
  });

  it("never offers all-countries to a single-country admin", () => {
    expect(resolveCountry(ALL_COUNTRIES, [gabon])).toBe("GA");
  });

  it("copes with an admin who has no country", () => {
    expect(resolveCountry("GA", [])).toBe(ALL_COUNTRIES);
  });
});

describe("countryLabel", () => {
  it("names the selected country or falls back to the all label", () => {
    expect(countryLabel("GA", [gabon, mali], "Tous")).toBe("Gabon");
    expect(countryLabel(ALL_COUNTRIES, [gabon, mali], "Tous")).toBe("Tous");
  });
});
