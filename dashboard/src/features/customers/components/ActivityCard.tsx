"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import type { CustomerDetail } from "../schemas";

type ActivityCardProps = {
  sessions: CustomerDetail["sessions"];
  currency: string;
  decimals: number;
};

export function ActivityCard({ sessions, currency, decimals }: ActivityCardProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.detail;

  return (
    <Card title={labels.activity}>
      <FactList
        facts={[
          { label: labels.completed, value: format.number(sessions.completed) },
          { label: labels.live, value: format.number(sessions.live) },
          { label: labels.energy, value: `${format.number(sessions.energyKwh, 1)} ${t.overview.unitKwh}` },
          { label: labels.spent, value: format.money([{ currency, decimals, minor: sessions.spentMinor }])[0] },
          { label: labels.lastSession, value: sessions.lastSessionAt ? format.dateTime(sessions.lastSessionAt) : labels.never },
        ]}
      />
    </Card>
  );
}
