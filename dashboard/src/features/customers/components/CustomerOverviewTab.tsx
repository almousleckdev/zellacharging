"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { CustomerDetail } from "../schemas";
import { IdentityCard } from "./IdentityCard";

export function CustomerOverviewTab({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.detail;
  const { wallet, sessions } = customer;
  const currency = wallet?.currency ?? "XAF";
  const decimals = wallet?.decimals ?? 0;
  const money = (minor: number) => format.money([{ currency, decimals, minor }])[0];

  return (
    <div className="flex flex-col gap-6">
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard label={labels.spent} value={money(sessions.spentMinor)} />
        <MetricCard label={labels.completed} value={format.number(sessions.completed)} hint={sessions.live > 0 ? labels.live : undefined} />
        <MetricCard label={labels.energy} value={`${format.number(sessions.energyKwh, 1)} ${t.overview.unitKwh}`} />
        <MetricCard label={labels.lastSession} value={sessions.lastSessionAt ? format.date(sessions.lastSessionAt) : labels.never} compact />
      </div>
      <IdentityCard customer={customer} />
    </div>
  );
}
