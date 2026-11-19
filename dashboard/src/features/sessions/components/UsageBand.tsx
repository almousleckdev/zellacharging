"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { MetricBand } from "@/shared/ui/MetricBand";
import { useUsage } from "../api/useUsage";
import type { UsageScope } from "../api/useUsage";
import { UsagePeriodCard } from "./UsagePeriodCard";

export function UsageBand({ scope = {} }: { scope?: UsageScope }) {
  const { t } = useI18n();
  const { country } = useSession();
  const usage = useUsage(country, scope);
  const labels = t.sessions.usage;
  const current = usage.data?.countries.find((item) => item.today.sessions > 0 || item.month.sessions > 0) ?? usage.data?.countries[0];

  if (!current) {
    return null;
  }

  return (
    <MetricBand columns={4}>
      <UsagePeriodCard title={labels.today} totals={current.today} country={current} />
      <UsagePeriodCard title={labels.yesterday} totals={current.yesterday} country={current} />
      <UsagePeriodCard title={labels.week} totals={current.week} country={current} />
      <UsagePeriodCard title={labels.month} totals={current.month} country={current} />
    </MetricBand>
  );
}
