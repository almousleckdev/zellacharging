import type { useFormatters } from "@/shared/i18n/useFormatters";
import type { WithdrawalItem } from "./schemas";

export function isPaidOut(row: WithdrawalItem): boolean {
  return row.state === "PAID";
}

export function withdrawalAmount(row: WithdrawalItem, format: ReturnType<typeof useFormatters>): string {
  const sign = isPaidOut(row) ? "− " : "";
  return sign + format.money([{ currency: row.currency, decimals: row.decimals, minor: row.amountMinor }])[0];
}
