"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { deltaPercent, periodsByCurrency } from "../seriesMath";
import type { SeriesResponse } from "../seriesSchema";

function deltaText(delta: number | null, activity: number, number: (value: number, digits?: number) => string, newLabel: string): string {
  if (activity === 0) {
    return "";
  }
  return delta === null ? newLabel : `${delta >= 0 ? "+" : "−"}${number(Math.abs(delta), 1)} %`;
}

export function CountryRevenue({ series, days }: { series: SeriesResponse; days: number }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.countries;
  const rows = series.countries.map((country) => {
    const period = periodsByCurrency({ countries: [country] }, days)[0];
    return { country, period, sales: period.current.capturedMinor, delta: deltaPercent(period.current.capturedMinor, period.previous.capturedMinor) };
  });
  const sameCurrency = new Set(rows.map((row) => row.period.currency)).size === 1;
  const top = Math.max(1, ...rows.map((row) => row.sales));

  return (
    <Card title={labels.title} description={labels.text}>
      <ul className="flex flex-col gap-5">
        {rows.map(({ country, period, sales, delta }) => (
          <li key={country.code} className="flex flex-col gap-2">
            <div className="flex items-baseline justify-between gap-3">
              <p className="text-sm font-semibold text-foreground">{country.name}</p>
              <p className="figure text-base font-semibold text-foreground">
                {format.money([{ currency: period.currency, decimals: period.decimals, minor: sales }])[0]}
              </p>
            </div>
            {sameCurrency && (
              <div role="img" aria-label={country.name} className="h-1.5 overflow-hidden rounded-full bg-surface-strong">
                <div className="h-full rounded-full bg-primary" style={{ width: `${Math.max(2, (sales / top) * 100)}%` }} />
              </div>
            )}
            <p className="flex justify-between text-xs text-subtle">
              <span>{interpolate(labels.charges, { count: format.number(period.current.sessions) })}</span>
              <span>{deltaText(delta, period.current.capturedMinor + period.previous.capturedMinor, format.number, labels.new)}</span>
            </p>
          </li>
        ))}
      </ul>
    </Card>
  );
}
