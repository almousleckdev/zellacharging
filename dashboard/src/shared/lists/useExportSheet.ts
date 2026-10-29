"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import type { ExportColumn, ExportSheet } from "@/shared/export/types";
import type { Column } from "@/shared/ui/DataTable";
import type { ListFilters } from "./filters";

export function useExportSheet<Row>(title: string, fileName: string, columns: Column<Row>[], filters: ListFilters) {
  const { t } = useI18n();
  const format = useFormatters();

  const exportColumns: ExportColumn<Row>[] = columns.flatMap((column) =>
    column.text ? [{ header: column.header, value: column.text, numeric: column.align === "right" }] : [],
  );
  const active = [
    filters.text && `"${filters.text}"`,
    filters.from && `${t.common.range.from} ${filters.from}`,
    filters.to && `${t.common.range.to} ${filters.to}`,
    ...Object.values(filters.extra).filter(Boolean),
  ].filter(Boolean);

  return (rows: Row[]): ExportSheet<Row> => ({
    title,
    fileName: `${fileName}-${new Date().toISOString().slice(0, 10)}`,
    columns: exportColumns,
    rows,
    subtitle: [
      interpolate(t.common.export.generated, { date: format.dateTime(new Date().toISOString()) }),
      interpolate(t.common.export.filters, { filters: active.length ? active.join(" · ") : t.common.export.noFilters }),
    ].join("  |  "),
  });
}
