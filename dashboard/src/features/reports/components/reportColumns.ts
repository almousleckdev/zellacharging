import type { SeriesPoint } from "@/features/overview/seriesSchema";

export type ReportCell = (point: SeriesPoint) => number;

export const MONEY_COLUMNS = ["topupsMinor", "withdrawalsMinor", "capturedMinor", "serviceFeeMinor", "vatMinor"] as const;
export const COUNT_COLUMNS = ["sessions", "completed", "failed"] as const;

export type ReportColumnKey = (typeof MONEY_COLUMNS)[number] | (typeof COUNT_COLUMNS)[number] | "energyKwh";

export const REPORT_KEYS: ReportColumnKey[] = [...MONEY_COLUMNS, ...COUNT_COLUMNS, "energyKwh"];

export function isMoney(key: ReportColumnKey): boolean {
  return (MONEY_COLUMNS as readonly string[]).includes(key);
}

export function totalOf(points: SeriesPoint[], key: ReportColumnKey): number {
  return points.reduce((sum, point) => sum + point[key], 0);
}
