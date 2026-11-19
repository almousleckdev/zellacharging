"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import type { UsageCountry, UsageTotals } from "../usageSchema";

type UsagePeriodCardProps = { title: string; totals: UsageTotals; country: UsageCountry };

export function UsagePeriodCard({ title, totals, country }: UsagePeriodCardProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.sessions.usage;
  const kwh = (value: number) => `${format.number(value, 1)} ${t.sessions.unitKwh}`;
  const money = format.money([{ currency: country.currency, decimals: country.decimals, minor: totals.collectedMinor }])[0];
  const rows = [
    { label: labels.charges, value: format.number(totals.sessions) },
    { label: labels.energy, value: kwh(totals.energyKwh) },
    { label: labels.collected, value: money },
  ];

  return (
    <article className="flex min-w-0 flex-col gap-2 border-b border-r border-border px-4 py-3.5">
      <h3 className="truncate text-[13px] text-muted">{title}</h3>
      <dl className="flex flex-col gap-1">
        {rows.map((row) => (
          <div key={row.label} className="flex items-baseline justify-between gap-2">
            <dt className="text-xs text-muted">{row.label}</dt>
            <dd className="figure font-display text-base font-semibold text-foreground">{row.value}</dd>
          </div>
        ))}
      </dl>
      {totals.staffEnergyKwh > 0 && (
        <p className="text-xs text-muted">{interpolate(labels.staff, { energy: kwh(totals.staffEnergyKwh) })}</p>
      )}
    </article>
  );
}
