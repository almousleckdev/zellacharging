"use client";

import { SettingsCard } from "@/features/platform/components/SettingsCard";
import { useSystem } from "@/features/platform/api/usePlatform";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Badge } from "@/shared/ui/Badge";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import { PageHeader } from "@/shared/ui/PageHeader";
import { QueryStatus } from "@/shared/ui/QueryStatus";

export function SystemPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile } = useSession();
  const query = useSystem(country);
  const labels = t.system;
  const probes: Record<string, string> = labels.probes;
  const fallback = profile.countries[0];
  const upCount = query.data?.probes.filter((probe) => probe.result.up).length ?? 0;

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        actions={
          <Button variant="secondary" loading={query.isFetching} onClick={() => void query.refetch()}>
            {labels.refresh}
          </Button>
        }
      />
      <QueryStatus pending={query.isPending} failed={query.isError} loadError={labels.loadError} onRetry={() => void query.refetch()} />
      {query.data && (
        <>
          <MetricBand columns={3}>
            <MetricCard label={labels.health} icon="shield" value={`${upCount} / ${query.data.probes.length}`} />
            <MetricCard label={labels.startedAt} icon="clipboard" value={format.dateTime(new Date(query.data.startedAtMillis).toISOString())} />
            <MetricCard label={labels.java} icon="sliders" value={query.data.javaVersion} />
          </MetricBand>
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
            {query.data.settings.map((section) => (
              <SettingsCard key={section.section} section={section} currency={fallback?.currency ?? "XAF"} decimals={fallback?.currencyDecimals ?? 0} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
