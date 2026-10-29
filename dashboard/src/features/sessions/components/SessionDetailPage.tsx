"use client";

import { useParams } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { DetailShell } from "@/shared/ui/DetailShell";
import { FactList } from "@/shared/ui/FactList";
import { CellLink } from "@/shared/ui/CellLink";
import { useSessionDetail } from "../api/useSessionDetail";
import { percentLabel } from "../format";
import { RelatedLinks } from "@/shared/ui/RelatedLinks";
import { SessionBilling } from "./SessionBilling";
import { SessionStateBadge } from "./SessionStateBadge";

export function SessionDetailPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const params = useParams<{ country: string; id: string }>();
  const query = useSessionDetail(params.country, params.id);
  const labels = t.sessions.detail;
  const data = query.data;
  const session = data?.session;
  const when = (iso: string | null) => (iso ? format.dateTime(iso) : t.common.none);
  const number = (value: number | null, digits: number, unit: string) => (value === null ? t.common.none : `${format.number(value, digits)} ${unit}`);

  return (
    <DetailShell
      backHref="/sessions"
      backLabel={labels.back}
      title={session ? `${session.stationId} · ${t.sessions.connector} ${session.connectorId}` : t.sessions.title}
      trail={[
        t.shell.breadcrumbRoot,
        ...(session
          ? [
              { label: t.customers.title, href: "/customers" },
              { label: session.customerName, href: `/customers/${session.country}/${session.customerId}` },
              { label: t.sessions.title, href: `/customers/${session.country}/${session.customerId}?tab=sessions` },
              session.stationId,
            ]
          : [t.sessions.title, "…"]),
      ]}
      icon="bolt"
      query={query}
      notFound={labels.notFound}
      loadError={labels.loadError}
      badge={session && <SessionStateBadge state={session.state} />}
    >
      {data && session && (
        <div className="grid items-start gap-6 lg:grid-cols-2">
          <Card title={labels.overview}>
            <FactList
              facts={[
                { label: labels.reference, value: <span className="font-mono text-xs">{session.id}</span> },
                {
                  label: labels.customer,
                  value: (
                    <CellLink href={scopedHref(`/customers/${session.country}/${session.customerId}`)} className="font-medium">
                      {session.customerName}
                    </CellLink>
                  ),
                },
                { label: labels.station, value: `${session.stationId} · ${t.sessions.connector} ${session.connectorId}` },
                { label: labels.idTag, value: <span className="font-mono text-xs">{data.idTag}</span> },
                { label: labels.transaction, value: data.transactionId ?? t.common.none },
                { label: labels.failure, value: session.failureCode ?? data.stopReason ?? t.common.none },
              ]}
            />
          </Card>
          <Card title={labels.energy}>
            <FactList
              facts={[
                { label: labels.delivered, value: number(session.energyKwh, 3, t.sessions.unitKwh) },
                { label: t.sessions.columns.batteryStart, value: percentLabel(session.startBatteryPercent) },
                { label: t.sessions.columns.batteryEnd, value: percentLabel(session.endBatteryPercent) },
                { label: labels.power, value: number(data.lastPowerKw, 1, "kW") },
                { label: labels.voltage, value: number(data.lastVoltage, 0, "V") },
                { label: labels.lastReading, value: when(data.progressAt) },
              ]}
            />
          </Card>
          <SessionBilling detail={data} />
          <RelatedLinks
            customerHref={`/customers/${session.country}/${session.customerId}`}
            links={[
              { label: labels.relatedCustomer, query: "" },
              { label: labels.relatedSessions, query: "?tab=sessions" },
              { label: labels.relatedWallet, query: "?tab=wallet" },
            ]}
          />
          <Card title={labels.timeline}>
            <FactList
              facts={[
                { label: labels.created, value: when(session.createdAt) },
                { label: labels.started, value: when(session.startedAt) },
                { label: labels.stopped, value: when(session.stoppedAt) },
                { label: labels.updated, value: when(data.updatedAt) },
              ]}
            />
          </Card>
        </div>
      )}
    </DetailShell>
  );
}
