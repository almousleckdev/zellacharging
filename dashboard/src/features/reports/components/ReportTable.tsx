"use client";

import type { CurrencySeries } from "@/features/overview/seriesMath";
import type { SeriesPoint } from "@/features/overview/seriesSchema";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { REPORT_KEYS } from "./reportColumns";
import { useReportFormat } from "./useReportFormat";

export function ReportTable({ group, bucket, caption }: { group: CurrencySeries; bucket: string; caption: string }) {
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

  return <DataTable caption={caption} columns={columns} rows={group.points} rowKey={(row) => row.day} />;
}
