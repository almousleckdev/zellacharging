"use client";

import { useSession } from "@/features/session/SessionProvider";
import { mergeByCurrency } from "@/features/overview/seriesMath";
import { ExportMenu } from "@/shared/export/ExportMenu";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { presetRange } from "@/shared/lib/dateRange";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { DateRangeFilter } from "@/shared/ui/DateRangeFilter";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Select } from "@/shared/ui/Select";
import { Skeleton } from "@/shared/ui/Skeleton";
import { useReport } from "../api/useReport";
import { ReportTable } from "./ReportTable";
import { useReportSheet } from "./useReportSheet";

const BUCKETS = ["day", "week", "month"] as const;

export function ReportsPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country } = useSession();
  const { params, update } = useUrlParams();
  const defaults = presetRange("last30");
  const from = params.get("from") ?? defaults.from;
  const to = params.get("to") ?? defaults.to;
  const bucket = (BUCKETS as readonly string[]).includes(params.get("bucket") ?? "") ? (params.get("bucket") as string) : "day";
  const report = useReport({ country, from, to, bucket });
  const groups = report.data ? mergeByCurrency(report.data) : [];
  const group = groups[0];
  const buckets: Record<string, string> = t.reports.buckets;
  const sheet = useReportSheet(group, bucket, from, to);

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={t.reports.title}
        subtitle={t.reports.subtitle}
        trail={[t.shell.breadcrumbRoot, t.reports.title]}
        actions={group && <ExportMenu load={async () => ({ rows: group.points, truncated: false })} sheet={sheet} limit={0} />}
      />
      <div className="flex flex-wrap items-center gap-4 rounded-card border border-border bg-background px-5 py-4">
        <DateRangeFilter from={from} to={to} onChange={(start, end) => update({ from: start, to: end }, false)} />
        <div className="w-44">
          <Select
            label={t.reports.bucketLabel}
            hideLabel
            options={BUCKETS.map((value) => ({ value, label: buckets[value] }))}
            value={bucket}
            onChange={(event) => update({ bucket: event.target.value }, false)}
          />
        </div>
      </div>
      {report.isPending && <Skeleton className="h-64" />}
      {report.isError && <ErrorState message={t.reports.loadError} onRetry={() => void report.refetch()} />}
      {report.data && !group && <p className="text-sm text-muted">{t.reports.empty}</p>}
      {group && <ReportTable group={group} bucket={bucket} />}
      {groups.length > 1 && <p className="text-sm text-muted">{t.overview.mixedCurrencies}</p>}
      <p className="sr-only">{format.number(groups.length)}</p>
    </div>
  );
}
