"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { deltaPercent } from "../seriesMath";
import type { CurrencyPeriod, Totals } from "../seriesMath";
import { Kpi } from "./Kpi";

export function KpiGrid({ period, days }: { period: CurrencyPeriod; days: number }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.kpi;
  const { current, previous } = period;
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];
  const deltaLabel = interpolate(labels.versus, { days });
  const rate = current.sessions === 0 ? 0 : (current.completed / current.sessions) * 100;

  const tiles: {
    key: string;
    icon: "wallet" | "percent" | "bolt" | "station" | "users" | "chart";
    value: string;
    hint: string;
    pick: (totals: Totals) => number;
  }[] = [
    { key: "sales", icon: "chart", value: money(current.capturedMinor), hint: labels.salesHint, pick: (totals) => totals.capturedMinor },
    { key: "commission", icon: "percent", value: money(current.serviceFeeMinor), hint: labels.commissionHint, pick: (totals) => totals.serviceFeeMinor },
    {
      key: "topups",
      icon: "wallet",
      value: money(current.topupsMinor),
      hint: interpolate(labels.topupsHint, { count: format.number(current.topupCount) }),
      pick: (totals) => totals.topupsMinor,
    },
    { key: "withdrawals", icon: "wallet", value: money(current.withdrawalsMinor), hint: labels.withdrawalsHint, pick: (totals) => totals.withdrawalsMinor },
    {
      key: "energy",
      icon: "station",
      value: `${format.number(current.energyKwh, 1)} ${t.overview.unitKwh}`,
      hint: interpolate(labels.energyHint, { count: format.number(current.completed) }),
      pick: (totals) => totals.energyKwh,
    },
    {
      key: "sessions",
      icon: "bolt",
      value: format.number(current.sessions),
      hint: interpolate(labels.sessionsHint, { rate: format.number(rate, 1), failed: format.number(current.failed) }),
      pick: (totals) => totals.sessions,
    },
  ];

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6">
      {tiles.map((tile) => (
        <Kpi
          key={tile.key}
          label={labels[tile.key as keyof typeof labels] as string}
          icon={tile.icon}
          value={tile.value}
          hint={tile.hint}
          delta={deltaPercent(tile.pick(current), tile.pick(previous))}
          deltaLabel={deltaLabel}
          newLabel={labels.new}
        />
      ))}
    </div>
  );
}
