"use client";

import { useMemo } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ExportMenu } from "@/shared/export/ExportMenu";
import type { ExportSheet } from "@/shared/export/types";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from "@/shared/lists/filters";
import { FilterSelect } from "@/shared/lists/FilterSelect";
import { ListToolbar } from "@/shared/lists/ListToolbar";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { DataTable } from "@/shared/ui/DataTable";
import { EmptyState } from "@/shared/ui/EmptyState";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Pagination } from "@/shared/ui/Pagination";
import { SearchInput } from "@/shared/ui/SearchInput";
import { TableSkeleton } from "@/shared/ui/TableSkeleton";
import { useStations } from "../api/useStations";
import { useStationColumns } from "./useStationColumns";

export function StationsPage() {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const { params, update } = useUrlParams();
  const query = useStations(country);
  const columns = useStationColumns();
  const text = params.get("q") ?? "";
  const status = params.get("status") ?? "";
  const requested = Number(params.get("size"));
  const size = (PAGE_SIZES as readonly number[]).includes(requested) ? requested : DEFAULT_PAGE_SIZE;
  const page = Math.max(0, Number(params.get("page")) || 0);

  const filtered = useMemo(
    () =>
      (query.data ?? []).filter(
        (row) =>
          (!text || row.station.id.toLowerCase().includes(text.toLowerCase())) &&
          (!status || (status === "online") === row.station.online),
      ),
    [query.data, text, status],
  );
  const visible = filtered.slice(page * size, page * size + size);
  const sheet = (rows: typeof filtered): ExportSheet<(typeof filtered)[number]> => ({
    title: t.stations.title,
    fileName: `stations-${new Date().toISOString().slice(0, 10)}`,
    subtitle: interpolate(t.stations.results, { count: rows.length }),
    columns: columns.flatMap((column) => (column.text ? [{ header: column.header, value: column.text, numeric: column.align === "right" }] : [])),
    rows,
  });

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={t.stations.title}
        subtitle={t.stations.subtitle}
        trail={[t.shell.breadcrumbRoot, t.stations.title]}
        actions={query.data && <p className="text-sm text-muted">{interpolate(t.stations.results, { count: filtered.length })}</p>}
      />
      <section className="overflow-hidden rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
        <ListToolbar
          search={<SearchInput label={t.stations.searchLabel} placeholder={t.stations.searchPlaceholder} value={text} onSearch={(value) => update({ q: value })} />}
          filters={
            <FilterSelect
              config={{
                key: "status",
                label: t.stations.statusLabel,
                allLabel: t.stations.allStatuses,
                options: [
                  { value: "online", label: t.stations.online },
                  { value: "offline", label: t.stations.offline },
                ],
              }}
              value={status}
              onChange={(value) => update({ status: value })}
            />
          }
          actions={<ExportMenu load={async () => ({ rows: filtered, truncated: false })} sheet={sheet} limit={0} />}
        />
        {query.isPending && <TableSkeleton />}
        {query.isError && (
          <div className="p-5">
            <ErrorState message={t.stations.loadError} onRetry={() => void query.refetch()} />
          </div>
        )}
        {query.data && filtered.length === 0 && (
          <div className="p-5">
            <EmptyState icon="station" title={t.stations.emptyTitle} text={t.stations.emptyText} />
          </div>
        )}
        {visible.length > 0 && (
          <DataTable
            caption={t.stations.title}
            columns={columns}
            rows={visible}
            rowKey={(row) => row.station.id}
            rowHref={(row) => scopedHref(`/stations/${encodeURIComponent(row.station.id)}`)}
          />
        )}
        <Pagination
          page={page}
          size={size}
          total={filtered.length}
          onPage={(next) => update({ page: next === 0 ? null : String(next) }, false)}
          onSize={(value) => update({ size: value === DEFAULT_PAGE_SIZE ? null : String(value) })}
        />
      </section>
    </div>
  );
}
