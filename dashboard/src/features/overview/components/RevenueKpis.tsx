"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { deltaPercent } from "../seriesMath";
import type { CurrencyPeriod, Totals } from "../seriesMath";
import { Kpi } from "./Kpi";

type Tile = { key: "sales" | "commission" | "energy" | "charges"; icon: "chart" | "bolt" | "percent" | "station"; value: string; hint: string; pick: (totals: Totals) => number };

export function RevenueKpis({ period, days }: { period: CurrencyPeriod; days: number }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.kpi;
  const { current, previous } = period;
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];
  const rate = current.sessions === 0 ? 0 : (current.completed / current.sessions) * 100;

  const tiles: Tile[] = [
    { key: "sales", icon: "chart", value: money(current.capturedMinor), hint: labels.salesHint, pick: (totals) => totals.capturedMinor },
    { key: "commission", icon: "percent", value: money(current.serviceFeeMinor), hint: labels.commissionHint, pick: (totals) => totals.serviceFeeMinor },
    {
      key: "energy",
      icon: "station",
      value: `${format.number(current.energyKwh, 1)} ${t.overview.unitKwh}`,
      hint: interpolate(labels.energyHint, { count: format.number(current.completed) }),
      pick: (totals) => totals.energyKwh,
    },
    {
      key: "charges",
      icon: "bolt",
      value: format.number(current.sessions),
      hint: interpolate(labels.chargesHint, { rate: format.number(rate, 1), failed: format.number(current.failed) }),
      pick: (totals) => totals.sessions,
    },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {tiles.map((tile) => (
        <Kpi
          key={tile.key}
          label={labels[tile.key]}
          icon={tile.icon}
          value={tile.value}
          hint={tile.hint}
          delta={deltaPercent(tile.pick(current), tile.pick(previous))}
          deltaLabel={interpolate(labels.versus, { days })}
          newLabel={labels.new}
        />
      ))}
    </div>
  );
}
