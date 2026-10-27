export type MoneyAmount = {
  currency: string;
  decimals: number;
  minor: number;
};

export type CurrencyDisplay = "symbol" | "code";

export function formatMoney(amount: MoneyAmount, locale: string, display: CurrencyDisplay = "symbol"): string {
  return new Intl.NumberFormat(locale, {
    style: "currency",
    currency: amount.currency,
    currencyDisplay: display,
    minimumFractionDigits: amount.decimals,
    maximumFractionDigits: amount.decimals,
  }).format(amount.minor / 10 ** amount.decimals);
}

export function formatMoneyLines(
  amounts: MoneyAmount[],
  locale: string,
  fallbackCurrency: string,
  display?: CurrencyDisplay,
): string[] {
  if (amounts.length === 0) {
    return [formatMoney({ currency: fallbackCurrency, decimals: 0, minor: 0 }, locale, display)];
  }
  const shown = display ?? (amounts.length > 1 ? "code" : "symbol");
  return amounts.map((amount) => formatMoney(amount, locale, shown));
}

export function formatNumber(value: number, locale: string, fractionDigits = 0): string {
  return new Intl.NumberFormat(locale, {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(value);
}

export function formatDate(iso: string, locale: string): string {
  return new Intl.DateTimeFormat(locale, { dateStyle: "medium" }).format(new Date(iso));
}

export function formatDateTime(iso: string, locale: string): string {
  return new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short" }).format(new Date(iso));
}
