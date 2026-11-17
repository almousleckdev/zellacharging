import type { useFormatters } from "@/shared/i18n/useFormatters";
import type { SessionItem } from "./schemas";

export function percentLabel(value: number | null): string {
  return value === null ? "" : `${Math.round(value)} %`;
}

export function sessionCost(row: SessionItem, format: ReturnType<typeof useFormatters>, fallbackCurrency: string): string {
  if (row.costMinor === null) {
    return "";
  }
  return format.money([{ currency: row.currency ?? fallbackCurrency, decimals: row.decimals ?? 0, minor: row.costMinor }])[0];
}

const LIVE_STATES = ["ACTIVE", "STOPPING"];
const LOST_CHARGER_WINDOW_MS = 3 * 60 * 60 * 1000;

export function canStopCharge(row: Pick<SessionItem, "state" | "failureCode" | "createdAt">, now: number = Date.now()): boolean {
  if (LIVE_STATES.includes(row.state)) {
    return true;
  }
  return row.state === "ABANDONED" && row.failureCode === "CHARGER_OFFLINE" && now - Date.parse(row.createdAt) < LOST_CHARGER_WINDOW_MS;
}
