"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { DataTable } from "@/shared/ui/DataTable";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import { PageHeader } from "@/shared/ui/PageHeader";
import { QueryStatus } from "@/shared/ui/QueryStatus";
import { Select } from "@/shared/ui/Select";
import { useStaffUsage } from "../api/useStaffUsage";
import { type Period, PERIODS } from "../periods";
import { useStaffColumns } from "./useStaffColumns";

export function StaffUsagePage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country } = useSession();
  const [period, setPeriod] = useState<Period>("month");
  const usage = useStaffUsage(country, period);
  const columns = useStaffColumns();
  const labels = t.staff;
  const rows = usage.data ?? [];
  const energy = rows.reduce((sum, row) => sum + row.energyKwh, 0);
  const charges = rows.reduce((sum, row) => sum + row.sessions, 0);
  const value = format.money(rows.map((row) => ({ currency: row.currency ?? "XAF", decimals: row.decimals ?? 0, minor: row.valueMinor })));
  const periods: Record<string, string> = labels.periods;

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        actions={
          <Select
            label={labels.period}
            hideLabel
            value={period}
            onChange={(event) => setPeriod(event.target.value as Period)}
            options={PERIODS.map((value) => ({ value, label: periods[value] }))}
          />
        }
      />
      <QueryStatus pending={usage.isPending} failed={usage.isError} loadError={labels.loadError} onRetry={() => void usage.refetch()} />
      {usage.data && (
        <>
          <MetricBand columns={3}>
            <MetricCard label={labels.energy} icon="bolt" value={`${format.number(energy, 1)} ${t.sessions.unitKwh}`} hint={labels.energyHint} />
            <MetricCard label={labels.value} icon="chart" value={value.length ? value.join(" · ") : format.number(0)} hint={labels.valueHint} />
            <MetricCard label={labels.charges} icon="users" value={format.number(charges)} hint={labels.chargesHint} />
          </MetricBand>
          {rows.length === 0 ? (
            <p className="text-sm text-muted">{labels.empty}</p>
          ) : (
            <DataTable caption={labels.title} columns={columns} rows={rows} rowKey={(row) => `${row.country}-${row.customerId}`} />
          )}
        </>
      )}
    </div>
  );
}
