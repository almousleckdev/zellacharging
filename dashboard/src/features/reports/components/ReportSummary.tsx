"use client";

import type { CurrencySeries } from "@/features/overview/seriesMath";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { IconName } from "@/shared/ui/Icon";
import { totalOf } from "./reportColumns";
import type { ReportColumnKey } from "./reportColumns";
import { useReportFormat } from "./useReportFormat";

const HEADLINES: { key: ReportColumnKey; icon: IconName }[] = [
  { key: "topupsMinor", icon: "wallet" },
  { key: "withdrawalsMinor", icon: "logout" },
  { key: "capturedMinor", icon: "chart" },
  { key: "serviceFeeMinor", icon: "percent" },
];

export function ReportSummary({ group, bucket }: { group: CurrencySeries; bucket: string }) {
  const { value, labels } = useReportFormat(group, bucket);

  return (
    <MetricBand>
      {HEADLINES.map(({ key, icon }) => (
        <MetricCard key={key} label={labels[key]} icon={icon} value={value(key, totalOf(group.points, key))} />
      ))}
    </MetricBand>
  );
}
