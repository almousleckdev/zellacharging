"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useSession } from "@/features/session/SessionProvider";
import type { Column } from "@/shared/ui/DataTable";
import type { StationView } from "../schemas";
import { StationOnlineBadge } from "./StationOnlineBadge";

export function useStationColumns(): Column<StationView>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const labels = t.stations.columns;
  const currency = (row: StationView) => profile.countries.find((item) => item.code === row.station.isoCode?.toUpperCase());
  const revenue = (row: StationView) => {
    const country = currency(row) ?? profile.countries[0];
    return format.money([{ currency: country?.currency ?? "XAF", decimals: country?.currencyDecimals ?? 0, minor: row.activity.revenueMinor }])[0];
  };
  const connectors = (row: StationView) => {
    const free = row.station.online ? row.station.connectors.filter((connector) => connector.status === "Available").length : 0;
    return `${free} / ${row.station.connectors.length}`;
  };
  const seen = (row: StationView) => (row.station.lastSeenAt ? format.dateTime(row.station.lastSeenAt) : t.common.none);
  const last = (row: StationView) => (row.activity.lastAt ? format.dateTime(row.activity.lastAt) : t.common.none);
  const model = (row: StationView) => [row.station.vendor, row.station.model].filter(Boolean).join(" ") || t.common.none;

  return [
    {
      key: "station",
      header: labels.station,
      text: (row) => `${row.station.id} (${model(row)}) ${row.station.isoCode ?? ""}`.trim(),
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{row.station.id}</span>
          <span className="text-xs text-subtle">{[model(row), row.station.isoCode].filter(Boolean).join(" · ")}</span>
        </span>
      ),
    },
    {
      key: "status",
      header: labels.status,
      text: (row) => `${row.station.online ? t.stations.online : t.stations.offline} ${seen(row)}`.trim(),
      cell: (row) => (
        <span className="flex flex-col items-start gap-1">
          <StationOnlineBadge online={row.station.online} />
          <span className="text-xs text-subtle">{seen(row)}</span>
        </span>
      ),
    },
    { key: "connectors", header: labels.connectors, align: "right", text: connectors, cell: connectors },
    { key: "sessions", header: labels.sessions, align: "right", text: (row) => row.activity.sessions, cell: (row) => format.number(row.activity.sessions) },
    {
      key: "energy",
      header: labels.energy,
      align: "right",
      text: (row) => `${format.number(row.activity.energyKwh, 1)} ${t.overview.unitKwh}`,
      cell: (row) => `${format.number(row.activity.energyKwh, 1)} ${t.overview.unitKwh}`,
    },
    { key: "revenue", header: labels.revenue, align: "right", text: revenue, cell: (row) => <span className="figure font-semibold">{revenue(row)}</span> },
    { key: "last", header: labels.lastCharge, align: "right", text: last, cell: last },
  ];
}
