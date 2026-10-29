"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { CurrencySeries } from "@/features/overview/seriesMath";
import type { SeriesPoint } from "@/features/overview/seriesSchema";
import type { ExportSheet } from "@/shared/export/types";
import { REPORT_KEYS } from "./reportColumns";
import { useReportFormat } from "./useReportFormat";

export function useReportSheet(group: CurrencySeries | undefined, bucket: string, from: string, to: string) {
  const { t } = useI18n();
  const { value, period, labels } = useReportFormat(group, bucket);

  return (rows: SeriesPoint[]): ExportSheet<SeriesPoint> => ({
    title: t.reports.title,
    fileName: `report-${from}-${to}`,
    subtitle: interpolate(t.reports.exportSubtitle, { from, to, currency: group?.currency ?? "" }),
    columns: [
      { header: labels.period, value: (row) => period(row.day) },
      ...REPORT_KEYS.map((key) => ({ header: labels[key], numeric: true, value: (row: SeriesPoint) => value(key, row[key]) })),
    ],
    rows,
  });
}
