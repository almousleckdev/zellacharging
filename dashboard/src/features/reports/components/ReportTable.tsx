"use client";

import type { CurrencySeries } from "@/features/overview/seriesMath";
import type { SeriesPoint } from "@/features/overview/seriesSchema";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { REPORT_KEYS, totalOf } from "./reportColumns";
import { useReportFormat } from "./useReportFormat";

export function ReportTable({ group, bucket }: { group: CurrencySeries; bucket: string }) {
  const { t } = useI18n();
  const { value, period, labels } = useReportFormat(group, bucket);

  const columns: Column<SeriesPoint>[] = [
    { key: "period", header: labels.period, cell: (row) => <span className="whitespace-nowrap font-medium">{period(row.day)}</span> },
    ...REPORT_KEYS.map((key) => ({
      key,
      header: labels[key],
      align: "right" as const,
      cell: (row: SeriesPoint) => value(key, row[key]),
    })),
  ];

  return (
    <Card title={`${t.reports.table} · ${group.currency}`}>
      <DataTable caption={t.reports.title} columns={columns} rows={group.points} rowKey={(row) => row.day} />
      <dl className="mt-4 grid gap-3 border-t border-border pt-4 text-sm sm:grid-cols-2 lg:grid-cols-4">
        {REPORT_KEYS.map((key) => (
          <div key={key} className="flex items-center justify-between gap-3">
            <dt className="text-muted">{labels[key]}</dt>
            <dd className="figure font-semibold">{value(key, totalOf(group.points, key))}</dd>
          </div>
        ))}
      </dl>
    </Card>
  );
}
