import type { CountryFigures } from "./schemas";

export type MoneySplit = {
  currency: string;
  decimals: number;
  heldMinor: number;
  owedMinor: number;
  earnedMinor: number;
  inFlightMinor: number;
};

export function moneySplits(countries: CountryFigures[]): MoneySplit[] {
  const byCurrency = new Map<string, MoneySplit>();
  for (const country of countries) {
    const known = byCurrency.get(country.currency);
    const held = country.wallets.topupsSucceededMinor - country.wallets.withdrawalsPaidMinor;
    const heldMinor = (known?.heldMinor ?? 0) + held;
    const owedMinor = (known?.owedMinor ?? 0) + country.wallets.balanceMinor;
    const earnedMinor = (known?.earnedMinor ?? 0) + country.wallets.capturedMinor;
    byCurrency.set(country.currency, {
      currency: country.currency,
      decimals: country.currencyDecimals,
      heldMinor,
      owedMinor,
      earnedMinor,
      inFlightMinor: Math.max(0, heldMinor - owedMinor - earnedMinor),
    });
  }
  return [...byCurrency.values()];
}

export function activeSplits(countries: CountryFigures[]): MoneySplit[] {
  const splits = moneySplits(countries);
  const active = splits.filter((split) => split.heldMinor !== 0 || split.owedMinor !== 0 || split.earnedMinor !== 0);
  return active.length > 0 ? active : splits.slice(0, 1);
}
