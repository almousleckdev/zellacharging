"use client";

import { useSession } from "@/features/session/SessionProvider";
import { SettingsCard } from "@/features/platform/components/SettingsCard";
import { TariffSummary } from "@/features/platform/components/TariffSummary";
import { useTariffs } from "@/features/platform/api/usePlatform";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { PageHeader } from "@/shared/ui/PageHeader";
import { QueryStatus } from "@/shared/ui/QueryStatus";

type Price = { stationId: string; pricePerKwhMinor: number; sessions: number; lastUsedAt: string | null };

export function TariffsPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile } = useSession();
  const query = useTariffs(country);
  const labels = t.tariffs;
  const fallback = profile.countries.find((item) => item.code === country) ?? profile.countries[0];
  const currency = fallback?.currency ?? "XAF";
  const decimals = fallback?.currencyDecimals ?? 0;

  const columns = (money: { currency: string; currencyDecimals: number }): Column<Price>[] => [
    { key: "station", header: labels.station, cell: (row) => <span className="font-medium">{row.stationId}</span> },
    {
      key: "price",
      header: labels.price,
      align: "right",
      cell: (row) => format.money([{ currency: money.currency, decimals: money.currencyDecimals, minor: row.pricePerKwhMinor }])[0],
    },
    { key: "sessions", header: labels.sessions, align: "right", cell: (row) => format.number(row.sessions) },
    { key: "last", header: labels.lastUsed, align: "right", cell: (row) => (row.lastUsedAt ? format.dateTime(row.lastUsedAt) : t.common.none) },
  ];

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title={labels.title} subtitle={labels.subtitle} />
      <QueryStatus pending={query.isPending} failed={query.isError} loadError={labels.loadError} onRetry={() => void query.refetch()} />
      {query.data && (
        <>
          <TariffSummary sections={query.data.settings} currency={currency} decimals={decimals} />
          <div className="grid items-start gap-6 lg:grid-cols-2">
            {query.data.settings.map((section) => (
              <SettingsCard key={section.section} section={section} currency={currency} decimals={decimals} />
            ))}
          </div>
          {query.data.stations.map((entry) => (
            <Card key={entry.country.code} title={`${labels.applied} · ${entry.country.name}`} description={labels.appliedText} flush>
              {entry.prices.length === 0 ? (
                <p className="px-5 pb-5 text-sm text-muted">{labels.noPrices}</p>
              ) : (
                <DataTable caption={labels.applied} columns={columns(entry.country)} rows={entry.prices} rowKey={(row) => `${row.stationId}-${row.pricePerKwhMinor}`} />
              )}
            </Card>
          ))}
        </>
      )}
    </div>
  );
}
