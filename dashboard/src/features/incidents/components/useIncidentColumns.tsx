"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import type { IncidentItem } from "../schemas";

const SEVERITY_TONES = { HIGH: "danger", MEDIUM: "warning", LOW: "neutral" } as const;
const STATUS_TONES = { OPEN: "danger", ACKNOWLEDGED: "warning", RESOLVED: "success" } as const;

export function subjectHref(incident: IncidentItem): string {
  return incident.subjectType === "session"
    ? `/sessions/${incident.country}/${incident.subjectId}`
    : `/payments/${incident.country}/${incident.subjectType.toUpperCase()}/${incident.subjectId}`;
}

export function useIncidentColumns(): Column<IncidentItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.incidents.columns;
  const kinds: Record<string, string> = t.incidents.kinds;
  const severities: Record<string, string> = t.incidents.severities;
  const statuses: Record<string, string> = t.incidents.statuses;
  const amount = (row: IncidentItem) =>
    row.amountMinor === null || row.currency === null
      ? ""
      : format.money([{ currency: row.currency, decimals: row.decimals, minor: row.amountMinor }])[0];

  return [
    {
      key: "severity",
      header: labels.severity,
      text: (row) => severities[row.severity] ?? row.severity,
      cell: (row) => <Badge tone={SEVERITY_TONES[row.severity as keyof typeof SEVERITY_TONES] ?? "neutral"}>{severities[row.severity] ?? row.severity}</Badge>,
    },
    {
      key: "incident",
      header: labels.incident,
      text: (row) => `${kinds[row.kind] ?? row.kind}${row.code ? ` (${row.code})` : ""}`,
      cell: (row) => (
        <CellLink href={scopedHref(subjectHref(row))} className="flex flex-col">
          <span className="font-medium text-foreground">{kinds[row.kind] ?? row.kind}</span>
          {row.code && <span className="font-mono text-xs text-subtle">{row.code}</span>}
        </CellLink>
      ),
    },
    {
      key: "customer",
      header: labels.customer,
      text: (row) => row.customerName,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="font-medium">
          {row.customerName}
        </CellLink>
      ),
    },
    { key: "amount", header: labels.amount, align: "right", text: amount, cell: amount },
    { key: "date", header: labels.date, align: "right", text: (row) => format.dateTime(row.occurredAt), cell: (row) => format.dateTime(row.occurredAt) },
    {
      key: "status",
      header: labels.status,
      text: (row) => statuses[row.status] ?? row.status,
      cell: (row) => <Badge tone={STATUS_TONES[row.status as keyof typeof STATUS_TONES] ?? "neutral"}>{statuses[row.status] ?? row.status}</Badge>,
    },
  ];
}
