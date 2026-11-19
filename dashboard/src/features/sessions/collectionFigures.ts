import type { SeriesPoint } from "@/features/overview/seriesSchema";

export type CollectionFigures = {
  today: SeriesPoint;
  yesterdayMinor: number;
  weekMinor: number;
  monthMinor: number;
};

const DAY_MS = 24 * 60 * 60 * 1000;

function isoDay(date: Date): string {
  return date.toISOString().slice(0, 10);
}

function startOfWeek(today: string): string {
  const date = new Date(`${today}T00:00:00Z`);
  const sinceMonday = (date.getUTCDay() + 6) % 7;
  return isoDay(new Date(date.getTime() - sinceMonday * DAY_MS));
}

function collectedSince(daily: SeriesPoint[], firstDay: string): number {
  return daily.filter((point) => point.day >= firstDay).reduce((total, point) => total + point.capturedMinor, 0);
}

export function collectionFigures(daily: SeriesPoint[], today: string): CollectionFigures {
  return {
    today: daily[daily.length - 1],
    yesterdayMinor: daily[daily.length - 2]?.capturedMinor ?? 0,
    weekMinor: collectedSince(daily, startOfWeek(today)),
    monthMinor: collectedSince(daily, `${today.slice(0, 7)}-01`),
  };
}
