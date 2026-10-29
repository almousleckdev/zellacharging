export type DayRange = { from: string; to: string };

export type RangePreset = "today" | "last7" | "last30" | "thisMonth";

const DAY_MS = 24 * 60 * 60 * 1000;

function isoDay(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

export function presetRange(preset: RangePreset, now = new Date()): DayRange {
  const today = isoDay(now);
  if (preset === "today") {
    return { from: today, to: today };
  }
  if (preset === "thisMonth") {
    return { from: isoDay(new Date(now.getFullYear(), now.getMonth(), 1)), to: today };
  }
  const span = preset === "last7" ? 6 : 29;
  return { from: isoDay(new Date(now.getTime() - span * DAY_MS)), to: today };
}

function startOfDay(day: string): Date {
  return new Date(`${day}T00:00:00`);
}

export function rangeToInstants(range: Partial<DayRange>): { from?: string; to?: string } {
  const result: { from?: string; to?: string } = {};
  if (range.from) {
    result.from = startOfDay(range.from).toISOString();
  }
  if (range.to) {
    const end = startOfDay(range.to);
    end.setDate(end.getDate() + 1);
    result.to = end.toISOString();
  }
  return result;
}
