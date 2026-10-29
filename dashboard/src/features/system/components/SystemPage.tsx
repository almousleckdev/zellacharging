"use client";

import { SettingsCard } from "@/features/platform/components/SettingsCard";
import { useSystem } from "@/features/platform/api/usePlatform";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Badge } from "@/shared/ui/Badge";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { ErrorState } from "@/shared/ui/ErrorState";
import { FactList } from "@/shared/ui/FactList";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Skeleton } from "@/shared/ui/Skeleton";

export function SystemPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile } = useSession();
  const query = useSystem(country);
  const labels = t.system;
  const probes: Record<string, string> = labels.probes;
  const fallback = profile.countries[0];

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        trail={[t.shell.breadcrumbRoot, labels.title]}
        actions={
          <Button variant="secondary" loading={query.isFetching} onClick={() => void query.refetch()}>
            {labels.refresh}
          </Button>
        }
      />
      {query.isPending && <Skeleton className="h-48" />}
      {query.isError && <ErrorState message={labels.loadError} onRetry={() => void query.refetch()} />}
      {query.data && (
        <div className="grid items-start gap-6 lg:grid-cols-2">
          <Card title={labels.health}>
            <FactList
              facts={query.data.probes.map((probe) => ({
                label: probes[probe.name] ?? probe.name,
                value: (
                  <span className="flex items-center justify-end gap-2">
                    <span className="text-xs text-subtle">{probe.result.millis} ms</span>
                    <Badge tone={probe.result.up ? "success" : "danger"}>{probe.result.up ? labels.up : labels.down}</Badge>
                  </span>
                ),
              }))}
            />
          </Card>
          <Card title={labels.runtime}>
            <FactList
              facts={[
                { label: labels.startedAt, value: format.dateTime(new Date(query.data.startedAtMillis).toISOString()) },
                { label: labels.java, value: query.data.javaVersion },
              ]}
            />
          </Card>
          {query.data.settings.map((section) => (
            <SettingsCard key={section.section} section={section} currency={fallback?.currency ?? "XAF"} decimals={fallback?.currencyDecimals ?? 0} />
          ))}
        </div>
      )}
    </div>
  );
}
