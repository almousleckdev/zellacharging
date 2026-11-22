"use client";

import { useSession } from "@/features/session/SessionProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ActionMenu } from "@/shared/ui/ActionMenu";
import type { ActionItem } from "@/shared/ui/ActionMenu";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { formatDay } from "../dates";
import type { Agreement } from "../schemas";
import { AgreementStatusBadge } from "./AgreementStatusBadge";

export type AgreementAction = "edit" | "suspend" | "resume" | "revoke";

type AgreementsTableProps = {
  items: Agreement[];
  onAction: (action: AgreementAction, agreement: Agreement) => void;
};

export function AgreementsTable({ items, onAction }: AgreementsTableProps) {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const labels = t.freeCharging;
  const eligibility: Record<string, string> = labels.eligibility;
  const canManage = profile.permissions.includes("FREE_CHARGING_MANAGE");

  function actionsOf(agreement: Agreement): ActionItem[] {
    if (!canManage || agreement.status === "REVOKED") {
      return [];
    }
    const toggle: AgreementAction = agreement.status === "SUSPENDED" ? "resume" : "suspend";
    return [
      { label: labels.actions.edit, onSelect: () => onAction("edit", agreement) },
      { label: labels.actions[toggle], onSelect: () => onAction(toggle, agreement) },
      { label: labels.actions.revoke, danger: true, onSelect: () => onAction("revoke", agreement) },
    ];
  }

  const columns: Column<Agreement>[] = [
    { key: "driver", header: labels.columns.driver, cell: (agreement) => <span className="font-medium">{agreement.driverName}</span> },
    { key: "startsOn", header: labels.columns.startsOn, cell: (agreement) => formatDay(agreement.startsOn, locale) },
    { key: "endsOn", header: labels.columns.endsOn, cell: (agreement) => (agreement.endsOn ? formatDay(agreement.endsOn, locale) : labels.noEndDate) },
    { key: "status", header: labels.columns.status, cell: (agreement) => <AgreementStatusBadge status={agreement.status} /> },
    { key: "eligibility", header: labels.columns.eligibility, cell: (agreement) => eligibility[agreement.eligibility] ?? labels.eligibilityUnknown },
    { key: "unpaid", header: labels.columns.unpaidDays, cell: (agreement) => (agreement.unpaidDays === null ? "" : format.number(agreement.unpaidDays)) },
    { key: "lastUse", header: labels.columns.lastUse, cell: (agreement) => (agreement.lastFreeUse ? format.dateTime(agreement.lastFreeUse) : labels.never) },
    { key: "conditions", header: labels.columns.conditions, cell: (agreement) => agreement.conditions || labels.noConditions },
    {
      key: "actions",
      header: "",
      align: "right",
      cell: (agreement) => {
        const menu = actionsOf(agreement);
        return menu.length > 0 ? <ActionMenu label={interpolate(labels.actions.menu, { name: agreement.driverName })} items={menu} /> : null;
      },
    },
  ];

  return <DataTable caption={labels.title} columns={columns} rows={items} rowKey={(agreement) => agreement.id} />;
}
