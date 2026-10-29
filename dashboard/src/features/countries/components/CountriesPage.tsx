"use client";

import Link from "next/link";
import { useOverview } from "@/features/overview/api/useOverview";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { ErrorState } from "@/shared/ui/ErrorState";
import { FactList } from "@/shared/ui/FactList";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Skeleton } from "@/shared/ui/Skeleton";

export function CountriesPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const overview = useOverview("ALL");
  const labels = t.countries;
  const allowed = new Set(profile.countries.map((item) => item.code));

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title={labels.title} subtitle={labels.subtitle} trail={[t.shell.breadcrumbRoot, labels.title]} />
      {overview.isPending && <Skeleton className="h-48" />}
      {overview.isError && <ErrorState message={labels.loadError} onRetry={() => void overview.refetch()} />}
      <div className="grid items-start gap-6 lg:grid-cols-2">
        {overview.data?.countries
          .filter((item) => allowed.has(item.code))
          .map((item) => {
            const money = (minor: number) => format.money([{ currency: item.currency, decimals: item.currencyDecimals, minor }], "code")[0];
            return (
              <Card
                key={item.code}
                title={`${item.name} (${item.code})`}
                action={
                  <Link href={`/?country=${item.code}`} className="text-sm font-medium text-primary-text hover:underline">
                    {labels.open}
                  </Link>
                }
              >
                <FactList
                  facts={[
                    { label: labels.currency, value: `${item.currency} (${item.currencyDecimals} ${labels.decimals})` },
                    { label: labels.phoneRegion, value: item.phoneRegion ?? t.common.none },
                    { label: labels.customers, value: `${format.number(item.customers.total)} · ${format.number(item.customers.active)} ${labels.active}` },
                    { label: labels.live, value: format.number(item.sessions.live) },
                    { label: labels.energy, value: `${format.number(item.sessions.energyKwh, 1)} ${t.overview.unitKwh}` },
                    { label: labels.sales, value: money(item.wallets.capturedMinor) },
                    { label: labels.balances, value: money(item.wallets.balanceMinor) },
                  ]}
                />
              </Card>
            );
          })}
      </div>
    </div>
  );
}
