export const PERIODS = ["today", "week", "month", "all"] as const;

export type Period = (typeof PERIODS)[number];

const DAYS: Record<Exclude<Period, "all">, number> = { today: 0, week: 6, month: 29 };

export function periodStart(period: Period, now: Date = new Date()): string | null {
  if (period === "all") {
    return null;
  }
  const start = new Date(now);
  start.setUTCHours(0, 0, 0, 0);
  start.setUTCDate(start.getUTCDate() - DAYS[period]);
  return start.toISOString();
}
