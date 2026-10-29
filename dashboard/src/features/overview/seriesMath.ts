import type { SeriesPoint, SeriesResponse } from "./seriesSchema";

export type Totals = Omit<SeriesPoint, "day">;

export type CurrencyPeriod = {
  currency: string;
  decimals: number;
  daily: SeriesPoint[];
  current: Totals;
  previous: Totals;
};

const DAY_MS = 24 * 60 * 60 * 1000;

const EMPTY: Totals = {
  topupsMinor: 0,
  topupCount: 0,
  withdrawalsMinor: 0,
  capturedMinor: 0,
  sessions: 0,
  completed: 0,
  failed: 0,
  energyKwh: 0,
  revenueMinor: 0,
  serviceFeeMinor: 0,
  vatMinor: 0,
};

function isoDay(date: Date): string {
  return date.toISOString().slice(0, 10);
}

function add(total: Totals, point: Totals): Totals {
  return {
    topupsMinor: total.topupsMinor + point.topupsMinor,
    topupCount: total.topupCount + point.topupCount,
    withdrawalsMinor: total.withdrawalsMinor + point.withdrawalsMinor,
    capturedMinor: total.capturedMinor + point.capturedMinor,
    sessions: total.sessions + point.sessions,
    completed: total.completed + point.completed,
    failed: total.failed + point.failed,
    energyKwh: total.energyKwh + point.energyKwh,
    revenueMinor: total.revenueMinor + point.revenueMinor,
    serviceFeeMinor: total.serviceFeeMinor + point.serviceFeeMinor,
    vatMinor: total.vatMinor + point.vatMinor,
  };
}

export function seriesWindow(days: number, now = new Date()): { from: string; to: string } {
  const today = new Date(`${isoDay(now)}T00:00:00Z`);
  return {
    from: new Date(today.getTime() - (days * 2 - 1) * DAY_MS).toISOString(),
    to: new Date(today.getTime() + DAY_MS).toISOString(),
  };
}

export function periodsByCurrency(response: SeriesResponse, days: number, now = new Date()): CurrencyPeriod[] {
  const today = new Date(`${isoDay(now)}T00:00:00Z`).getTime();
  const groups = new Map<string, { decimals: number; byDay: Map<string, SeriesPoint> }>();
  for (const country of response.countries) {
    const group = groups.get(country.currency) ?? { decimals: country.decimals, byDay: new Map() };
    for (const point of country.points) {
      const known = group.byDay.get(point.day);
      group.byDay.set(point.day, known ? { day: point.day, ...add(known, point) } : point);
    }
    groups.set(country.currency, group);
  }
  return [...groups.entries()].map(([currency, group]) => {
    const all = Array.from({ length: days * 2 }, (_, index) => {
      const day = isoDay(new Date(today - (days * 2 - 1 - index) * DAY_MS));
      return group.byDay.get(day) ?? { day, ...EMPTY };
    });
    const sum = (points: SeriesPoint[]) => points.reduce<Totals>((total, point) => add(total, point), EMPTY);
    return {
      currency,
      decimals: group.decimals,
      daily: all.slice(days),
      current: sum(all.slice(days)),
      previous: sum(all.slice(0, days)),
    };
  });
}

export function deltaPercent(current: number, previous: number): number | null {
  if (previous === 0) {
    return current === 0 ? 0 : null;
  }
  return ((current - previous) / previous) * 100;
}

export type CurrencySeries = { currency: string; decimals: number; points: SeriesPoint[] };

export function mergeByCurrency(response: SeriesResponse): CurrencySeries[] {
  const groups = new Map<string, { decimals: number; byDay: Map<string, SeriesPoint> }>();
  for (const country of response.countries) {
    const group = groups.get(country.currency) ?? { decimals: country.decimals, byDay: new Map() };
    for (const point of country.points) {
      const known = group.byDay.get(point.day);
      group.byDay.set(point.day, known ? { day: point.day, ...add(known, point) } : point);
    }
    groups.set(country.currency, group);
  }
  return [...groups.entries()].map(([currency, group]) => ({
    currency,
    decimals: group.decimals,
    points: [...group.byDay.values()].sort((a, b) => a.day.localeCompare(b.day)),
  }));
}
