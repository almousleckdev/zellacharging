"use client";

import { useSession } from "@/features/session/SessionProvider";
import { mergeByCurrency } from "@/features/overview/seriesMath";
import { ExportMenu } from "@/shared/export/ExportMenu";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ListToolbar } from "@/shared/lists/ListToolbar";
import { presetRange } from "@/shared/lib/dateRange";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { DateRangeFilter } from "@/shared/ui/DateRangeFilter";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Select } from "@/shared/ui/Select";
import { useReport } from "../api/useReport";
import { ReportSummary } from "./ReportSummary";
import { ReportTable } from "./ReportTable";
import { useReportSheet } from "./useReportSheet";

const BUCKETS = ["day", "week", "month"] as const;

export function ReportsPage() {
  const { t } = useI18n();
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
      <PageHeader title={t.reports.title} subtitle={t.reports.subtitle} />
      {group && <ReportSummary group={group} bucket={bucket} />}
      <ListPanel
        toolbar={
          <ListToolbar
            search={null}
            range={<DateRangeFilter from={from} to={to} onChange={(start, end) => update({ from: start, to: end }, false)} />}
            filters={
              <div className="w-full sm:w-44">
                <Select
                  label={t.reports.bucketLabel}
                  hideLabel
                  options={BUCKETS.map((value) => ({ value, label: buckets[value] }))}
                  value={bucket}
                  onChange={(event) => update({ bucket: event.target.value }, false)}
                />
              </div>
            }
            actions={group && <ExportMenu load={async () => ({ rows: group.points, truncated: false })} sheet={sheet} limit={0} />}
          />
        }
        pending={report.isPending}
        failed={report.isError}
        onRetry={() => void report.refetch()}
        loadError={t.reports.loadError}
        empty={{ icon: "chart", title: t.reports.empty, text: "" }}
        isEmpty={report.data !== undefined && !group}
      >
        {group && <ReportTable group={group} bucket={bucket} caption={t.reports.title} />}
      </ListPanel>
    </div>
  );
}
