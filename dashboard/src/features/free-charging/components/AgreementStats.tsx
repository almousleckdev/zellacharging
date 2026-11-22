"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { Agreement } from "../schemas";

export function AgreementStats({ items }: { items: Agreement[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.freeCharging.stats;
  const active = items.filter((agreement) => agreement.status !== "REVOKED");

  return (
    <MetricBand columns={3}>
      <MetricCard label={labels.inProgram} value={format.number(active.length)} icon="users" />
      <MetricCard label={labels.eligible} value={format.number(active.filter((agreement) => agreement.eligibility === "ELIGIBLE").length)} icon="bolt" />
      <MetricCard label={labels.toRegularize} value={format.number(active.filter((agreement) => agreement.eligibility === "UNPAID_DAYS").length)} icon="wallet" />
    </MetricBand>
  );
}
