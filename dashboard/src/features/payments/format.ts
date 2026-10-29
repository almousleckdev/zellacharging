import type { useFormatters } from "@/shared/i18n/useFormatters";
import type { PaymentItem } from "./schemas";

const SETTLED = new Set(["SUCCEEDED", "PAID"]);

export function isSettled(row: PaymentItem): boolean {
  return SETTLED.has(row.state);
}

export function signedAmount(row: PaymentItem, format: ReturnType<typeof useFormatters>): string {
  const sign = isSettled(row) ? (row.kind === "WITHDRAWAL" ? "− " : "+ ") : "";
  return sign + format.money([{ currency: row.currency, decimals: row.decimals, minor: row.amountMinor }])[0];
}
