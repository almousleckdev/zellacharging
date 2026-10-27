import type { Country } from "./schemas";

export const ALL_COUNTRIES = "ALL";

export function resolveCountry(requested: string | null, countries: Country[]): string {
  if (countries.length === 0) {
    return ALL_COUNTRIES;
  }
  if (requested === ALL_COUNTRIES && countries.length > 1) {
    return ALL_COUNTRIES;
  }
  const match = countries.find((country) => country.code === requested);
  if (match) {
    return match.code;
  }
  return countries.length > 1 ? ALL_COUNTRIES : countries[0].code;
}

export function countryLabel(scope: string, countries: Country[], allLabel: string): string {
  return countries.find((country) => country.code === scope)?.name ?? allLabel;
}
