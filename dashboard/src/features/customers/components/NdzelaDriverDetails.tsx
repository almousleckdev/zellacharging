"use client";

import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Button } from "@/shared/ui/Button";
import { FactList } from "@/shared/ui/FactList";
import type { Fact } from "@/shared/ui/FactList";
import { Skeleton } from "@/shared/ui/Skeleton";
import { useNdzelaDriver } from "../api/useNdzelaDriver";
import type { CustomerDetail } from "../schemas";

export function NdzelaDriverDetails({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.support.ndzela.driver;
  const query = useNdzelaDriver(customer.country, customer.id);
  const currency = customer.wallet?.currency ?? "XAF";
  const decimals = customer.wallet?.decimals ?? 0;
  const rent = (amount: number) => format.money([{ currency, decimals, minor: amount * 10 ** decimals }])[0];

  if (query.isPending) {
    return <Skeleton className="h-32" />;
  }
  if (query.isError) {
    const code = query.error instanceof ApiError ? query.error.code : "UNKNOWN";
    return (
      <div className="flex flex-wrap items-center justify-between gap-3 text-sm text-muted">
        <p>{errorMessage(t, code)}</p>
        {code === "NDZELA_UNAVAILABLE" && (
          <Button variant="secondary" onClick={() => void query.refetch()}>
            {t.common.retry}
          </Button>
        )}
      </div>
    );
  }

  const { driver, contract, car } = query.data;
  const sections: { title: string; facts: Fact[] }[] = [
    {
      title: labels.person,
      facts: [
        { label: labels.name, value: `${driver.firstName} ${driver.lastName}` },
        { label: labels.phone, value: driver.phone ?? "" },
        { label: labels.status, value: driver.status },
      ],
    },
    {
      title: labels.contract,
      facts: contract
        ? [
            { label: labels.contractNumber, value: contract.number },
            { label: labels.status, value: contract.status },
            { label: labels.period, value: `${format.date(contract.startDate)} → ${format.date(contract.endDate)}` },
            { label: labels.dailyRent, value: rent(contract.dailyRent) },
          ]
        : [],
    },
    {
      title: labels.car,
      facts: car
        ? [
            { label: labels.carNumber, value: car.fleetNumber },
            { label: labels.plate, value: car.plate },
            { label: labels.model, value: `${car.brand} ${car.model} ${car.year}` },
            { label: labels.color, value: car.color ?? "" },
          ]
        : [],
    },
  ];

  return (
    <div className="flex flex-col gap-5 border-t border-border pt-4">
      {sections.map((section) => (
        <section key={section.title} className="flex flex-col gap-2">
          <h3 className="text-xs font-semibold uppercase tracking-wide text-subtle">{section.title}</h3>
          {section.facts.length > 0 ? <FactList facts={section.facts} /> : <p className="text-sm text-muted">{labels.none}</p>}
        </section>
      ))}
    </div>
  );
}
