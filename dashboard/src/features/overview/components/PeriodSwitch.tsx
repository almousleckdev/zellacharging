"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";

export const PERIODS = [7, 30, 90] as const;

type PeriodSwitchProps = { days: number; onChange: (days: number) => void };

export function PeriodSwitch({ days, onChange }: PeriodSwitchProps) {
  const { t } = useI18n();

  return (
    <div role="group" aria-label={t.overview.period.label} className="flex overflow-hidden rounded-control border border-border-strong">
      {PERIODS.map((value) => (
        <button
          key={value}
          type="button"
          aria-pressed={days === value}
          onClick={() => onChange(value)}
          className={cn(
            "h-9 border-r border-border-strong px-3.5 text-sm last:border-r-0",
            days === value ? "bg-primary-soft font-semibold text-primary-text" : "bg-background text-muted hover:bg-surface",
          )}
        >
          {value} {t.overview.period.days}
        </button>
      ))}
    </div>
  );
}
