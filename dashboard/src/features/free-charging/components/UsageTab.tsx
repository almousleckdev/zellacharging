"use client";

import { useState } from "react";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { ListPanel } from "@/shared/ui/ListPanel";
import { USAGE_PAGE_SIZE, useFreeUsage } from "../api/useFreeUsage";
import type { FreeUsage } from "../schemas";

export function UsageTab() {
  const { t } = useI18n();
  const format = useFormatters();
  const [page, setPage] = useState(0);
  const usage = useFreeUsage(page);
  const labels = t.freeCharging.usage;
  const items = usage.data?.items ?? [];

  const columns: Column<FreeUsage>[] = [
    { key: "driver", header: labels.columns.driver, cell: (row) => <span className="font-medium">{row.driverName}</span> },
    { key: "startedAt", header: labels.columns.startedAt, cell: (row) => format.dateTime(row.startedAt) },
    { key: "energy", header: labels.columns.energy, cell: (row) => (row.energyKwh === null ? "" : `${format.number(row.energyKwh, 1)} ${labels.unitKwh}`) },
    { key: "stopReason", header: labels.columns.stopReason, cell: (row) => row.stopReason ?? "" },
  ];

  return (
    <ListPanel
      pending={usage.isPending}
      failed={usage.isError}
      onRetry={() => void usage.refetch()}
      loadError={labels.loadError}
      empty={{ icon: "clipboard", title: labels.emptyTitle, text: labels.emptyText }}
      isEmpty={usage.isSuccess && items.length === 0}
      page={page}
      size={USAGE_PAGE_SIZE}
      total={usage.data?.total ?? 0}
      onPage={setPage}
    >
      {items.length > 0 && <DataTable caption={t.freeCharging.tabs.history} columns={columns} rows={items} rowKey={(row) => row.sessionId} />}
    </ListPanel>
  );
}
