"use client";

import { useParams } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { SessionsByCustomerPage } from "@/features/sessions/components/SessionsByCustomerPage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { DetailShell } from "@/shared/ui/DetailShell";
import { FactList } from "@/shared/ui/FactList";
import { useStation } from "../api/useStations";
import type { Connector } from "../schemas";
import { ConnectorStatusBadge } from "./ConnectorStatusBadge";
import { StationOnlineBadge } from "./StationOnlineBadge";

export function StationDetailPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile } = useSession();
  const params = useParams<{ id: string }>();
  const id = decodeURIComponent(params.id);
  const query = useStation(country, id);
  const labels = t.stations.detail;
  const data = query.data;
  const station = data?.station;
  const currency = profile.countries.find((item) => item.code === station?.isoCode?.toUpperCase()) ?? profile.countries[0];
  const money = (minor: number) => format.money([{ currency: currency?.currency ?? "XAF", decimals: currency?.currencyDecimals ?? 0, minor }])[0];
  const when = (iso: string | null) => (iso ? format.dateTime(iso) : t.common.none);

  const columns: Column<Connector>[] = [
    { key: "id", header: labels.connector, cell: (row) => <span className="font-medium">{row.connectorId}</span> },
    { key: "type", header: labels.type, cell: (row) => row.connectorType ?? t.common.none },
    { key: "power", header: labels.power, align: "right", cell: (row) => (row.maxPowerKw === null ? t.common.none : `${format.number(row.maxPowerKw, 0)} kW`) },
    { key: "status", header: labels.status, align: "right", cell: (row) => <ConnectorStatusBadge status={row.status} /> },
  ];

  return (
    <DetailShell
      backHref="/stations"
      backLabel={labels.back}
      title={id}
      trail={[t.shell.breadcrumbRoot, { label: t.stations.title, href: "/stations" }, id]}
      icon="station"
      query={query}
      notFound={labels.notFound}
      loadError={labels.loadError}
      badge={station && <StationOnlineBadge online={station.online} />}
    >
      {data && station && (
        <div className="flex flex-col gap-6">
          <div className="grid items-start gap-6 lg:grid-cols-2">
            <Card title={labels.station}>
              <FactList
                facts={[
                  { label: labels.country, value: station.isoCode ?? t.common.none },
                  { label: labels.vendor, value: station.vendor ?? t.common.none },
                  { label: labels.model, value: station.model ?? t.common.none },
                  { label: labels.lastSeen, value: when(station.lastSeenAt) },
                  {
                    label: labels.location,
                    value:
                      station.latitude !== null && station.longitude !== null ? (
                        <a
                          href={`https://www.google.com/maps?q=${station.latitude},${station.longitude}`}
                          target="_blank"
                          rel="noreferrer"
                          className="text-primary-text hover:underline"
                        >
                          {station.latitude}, {station.longitude}
                        </a>
                      ) : (
                        t.common.none
                      ),
                  },
                ]}
              />
            </Card>
            <Card title={labels.activity}>
              <FactList
                facts={[
                  { label: labels.sessions, value: format.number(data.activity.sessions) },
                  { label: labels.live, value: format.number(data.activity.live) },
                  { label: labels.energy, value: `${format.number(data.activity.energyKwh, 1)} ${t.overview.unitKwh}` },
                  { label: labels.revenue, value: <span className="figure font-semibold">{money(data.activity.revenueMinor)}</span> },
                  { label: labels.lastCharge, value: when(data.activity.lastAt) },
                ]}
              />
            </Card>
          </div>
          <Card title={labels.connectors}>
            <DataTable caption={labels.connectors} columns={columns} rows={station.connectors} rowKey={(row) => String(row.connectorId)} />
          </Card>
          <section className="flex flex-col gap-3">
            <h2 className="font-display text-base font-semibold text-foreground">{labels.customers}</h2>
            <SessionsByCustomerPage stationId={id} />
          </section>
        </div>
      )}
    </DetailShell>
  );
}
