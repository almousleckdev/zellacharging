import type { CurrencyPeriod } from "./seriesMath";
import type { SeriesPoint } from "./seriesSchema";

const WEEK = 7;

export type TodayFigures = {
  today: SeriesPoint;
  yesterday: SeriesPoint | undefined;
  week: SeriesPoint[];
};

export function todayFigures(period: CurrencyPeriod): TodayFigures {
  const daily = period.daily;
  return {
    today: daily[daily.length - 1],
    yesterday: daily[daily.length - 2],
    week: daily.slice(-WEEK),
  };
}
