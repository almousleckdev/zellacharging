"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useStations } from "@/features/stations/api/useStations";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { Skeleton } from "@/shared/ui/Skeleton";

export function NetworkHealth() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const stations = useStations(country);
  const labels = t.overview.network;
  const currency = profile.countries.find((item) => item.code === country) ?? profile.countries[0];
  const all = stations.data ?? [];
  const online = all.filter((row) => row.station.online).length;
  const top = [...all].sort((a, b) => b.activity.revenueMinor - a.activity.revenueMinor).filter((row) => row.activity.sessions > 0).slice(0, 5);

  return (
    <Card
      title={labels.title}
      action={
        <Link href={scopedHref("/stations")} className="text-sm font-medium text-primary-text hover:underline">
          {labels.all}
        </Link>
      }
    >
      {stations.isPending && <Skeleton className="h-32" />}
      {stations.isError && <p className="text-sm text-muted">{labels.unavailable}</p>}
      {stations.data && (
        <div className="flex flex-col gap-5">
          <p className="figure font-display text-2xl font-semibold text-foreground">
            {online} <span className="text-base font-normal text-subtle">/ {all.length} {labels.online}</span>
          </p>
          {top.length > 0 && (
            <ul className="flex flex-col divide-y divide-border">
              {top.map((row) => (
                <li key={row.station.id} className="flex items-center justify-between gap-3 py-2 text-sm">
                  <Link href={scopedHref(`/stations/${encodeURIComponent(row.station.id)}`)} className="truncate font-medium hover:text-primary-text">
                    {row.station.id}
                  </Link>
                  <span className="figure shrink-0 text-muted">
                    {interpolate(labels.charges, { count: format.number(row.activity.sessions) })} ·{" "}
                    {format.money([{ currency: currency?.currency ?? "XAF", decimals: currency?.currencyDecimals ?? 0, minor: row.activity.revenueMinor }])[0]}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </Card>
  );
}
