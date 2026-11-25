"use client";

import type { ReactNode } from "react";
import type { z } from "zod";
import { ExportMenu } from "@/shared/export/ExportMenu";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { DateRangeFilter } from "@/shared/ui/DateRangeFilter";
import type { IconName } from "@/shared/ui/Icon";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";
import { SearchInput } from "@/shared/ui/SearchInput";
import { EXPORT_MAX_ROWS } from "./filters";
import { fetchAllRows } from "./fetchAllRows";
import { FilterSelect } from "./FilterSelect";
import type { FilterSelectConfig } from "./FilterSelect";
import { ListToolbar } from "./ListToolbar";
import { useExportSheet } from "./useExportSheet";
import { useListFilters } from "./useListFilters";
import { useLogList } from "./useLogList";

export type LogListLabels = {
  title: string;
  subtitle: string;
  results: string;
  loadError: string;
  emptyTitle: string;
  emptyText: string;
  searchLabel: string;
  searchPlaceholder: string;
};

type Listing<Row> = { total: number; items: Row[] };

type LogListPageProps<Row, Out extends Listing<Row>> = {
  resource: string;
  schema: z.ZodType<Out>;
  labels: LogListLabels;
  icon: IconName;
  columns: Column<Row>[];
  rowKey: (row: Row) => string;
  rowHref?: (row: Row) => string;
  filters?: FilterSelectConfig[];
  fixed?: Record<string, string>;
  withRange?: boolean;
  embedded?: boolean;
  searchable?: boolean;
  exportName?: string;
  rowActions?: Column<Row>;
  above?: (data: Out) => ReactNode;
  headerActions?: ReactNode;
  refreshMs?: number;
};

export function LogListPage<Row, Out extends Listing<Row>>(props: LogListPageProps<Row, Out>) {
  const { resource, schema, labels, icon, columns, rowKey, rowHref, fixed, above } = props;
  const selects = props.filters ?? [];
  const format = useFormatters();
  const list = useListFilters(selects.map((select) => select.key), fixed);
  const { filters } = list;
  const query = useLogList(resource, filters, schema, props.refreshMs);
  const sheet = useExportSheet(labels.title, props.exportName ?? resource, columns, filters);
  const data = query.data;

  const toolbar = (
    <ListToolbar
      search={props.searchable === false ? null : <SearchInput label={labels.searchLabel} placeholder={labels.searchPlaceholder} value={filters.text} onSearch={list.setText} />}
      filters={selects.map((select) => (
        <FilterSelect key={select.key} config={select} value={filters.extra[select.key]} onChange={(value) => list.setExtra(select.key, value)} />
      ))}
      actions={<ExportMenu load={() => fetchAllRows<Row, Out>(resource, filters, schema)} sheet={sheet} limit={EXPORT_MAX_ROWS} />}
      range={props.withRange === false ? undefined : <DateRangeFilter from={filters.from} to={filters.to} onChange={list.setRange} />}
    />
  );

  const panel = (
    <ListPanel
      toolbar={toolbar}
      pending={query.isPending}
      failed={query.isError}
      onRetry={() => void query.refetch()}
      loadError={labels.loadError}
      empty={{ icon, title: labels.emptyTitle, text: labels.emptyText }}
      isEmpty={data?.items.length === 0}
      page={filters.page}
      size={filters.size}
      total={data?.total ?? 0}
      onPage={list.setPage}
      onSize={list.setSize}
    >
      {data && data.items.length > 0 && (
        <DataTable
          caption={labels.title}
          columns={props.rowActions ? [...columns, props.rowActions] : columns}
          rows={data.items}
          rowKey={rowKey}
          rowHref={rowHref}
          sort={{ value: filters.sort, onChange: list.setSort }}
        />
      )}
    </ListPanel>
  );

  if (props.embedded) {
    return panel;
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        actions={
          <>
            {data && <p className="text-sm text-muted">{interpolate(labels.results, { count: format.number(data.total) })}</p>}
            {props.headerActions}
          </>
        }
      />
      {data && above?.(data)}
      {panel}
    </div>
  );
}
