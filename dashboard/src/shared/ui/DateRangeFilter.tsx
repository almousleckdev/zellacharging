"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { presetRange } from "@/shared/lib/dateRange";
import type { RangePreset } from "@/shared/lib/dateRange";

type DateRangeFilterProps = {
  from: string;
  to: string;
  onChange: (from: string, to: string) => void;
};

const PRESETS: RangePreset[] = ["today", "last7", "last30", "thisMonth"];
const FIELD = "h-9 rounded-control border border-border-strong bg-background px-2.5 text-sm text-foreground hover:border-subtle";

export function DateRangeFilter({ from, to, onChange }: DateRangeFilterProps) {
  const { t } = useI18n();
  const labels = t.common.range;
  const active = PRESETS.find((preset) => {
    const range = presetRange(preset);
    return range.from === from && range.to === to;
  });

  return (
    <div role="group" aria-label={labels.label} className="flex flex-wrap items-center gap-2">
      <div className="flex overflow-hidden rounded-control border border-border-strong">
        {PRESETS.map((preset) => (
          <button
            key={preset}
            type="button"
            aria-pressed={active === preset}
            onClick={() => {
              const range = presetRange(preset);
              onChange(range.from, range.to);
            }}
            className={cn(
              "h-9 border-r border-border-strong px-3 text-sm last:border-r-0",
              active === preset ? "bg-primary-soft font-semibold text-primary-text" : "bg-background text-muted hover:bg-surface",
            )}
          >
            {labels[preset]}
          </button>
        ))}
      </div>
      <label className="flex items-center gap-1.5 text-sm text-muted">
        <span>{labels.from}</span>
        <input type="date" value={from} max={to || undefined} onChange={(event) => onChange(event.target.value, to)} className={FIELD} />
      </label>
      <label className="flex items-center gap-1.5 text-sm text-muted">
        <span>{labels.to}</span>
        <input type="date" value={to} min={from || undefined} onChange={(event) => onChange(from, event.target.value)} className={FIELD} />
      </label>
      {(from || to) && (
        <button type="button" onClick={() => onChange("", "")} className="text-sm font-medium text-primary-text hover:underline">
          {labels.clear}
        </button>
      )}
    </div>
  );
}
