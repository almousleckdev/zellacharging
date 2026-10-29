import type { useFormatters } from "@/shared/i18n/useFormatters";
import type { SessionItem } from "./schemas";

export function percentLabel(value: number | null): string {
  return value === null ? "–" : `${Math.round(value)} %`;
}

export function sessionCost(row: SessionItem, format: ReturnType<typeof useFormatters>, fallbackCurrency: string): string {
  if (row.costMinor === null) {
    return "–";
  }
  return format.money([{ currency: row.currency ?? fallbackCurrency, decimals: row.decimals ?? 0, minor: row.costMinor }])[0];
}
