"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import { useCustomerCounts } from "../api/useCustomerCounts";

export function CustomerCountsBand() {
  const { t } = useI18n();
  const { country } = useSession();
  const format = useFormatters();
  const counts = useCustomerCounts(country);
  const labels = t.customers.counts;

  if (!counts.data) {
    return null;
  }

  const cards = [
    { label: labels.total, value: counts.data.total, icon: "users" },
    { label: labels.publicUsers, value: counts.data.publicUsers, icon: "user" },
    { label: labels.ndzelaDrivers, value: counts.data.ndzelaDrivers, icon: "user" },
    { label: labels.ndzelaStaff, value: counts.data.ndzelaStaff, icon: "user" },
    { label: labels.suspended, value: counts.data.suspended, icon: "user" },
  ] as const;

  return (
    <MetricBand columns={5}>
      {cards.map((card) => (
        <MetricCard key={card.label} label={card.label} value={format.number(card.value)} icon={card.icon} />
      ))}
    </MetricBand>
  );
}
