"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import type { StaffUsage } from "../schemas";

export function useStaffColumns(): Column<StaffUsage>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.staff.columns;
  const energy = (row: StaffUsage) => `${format.number(row.energyKwh, 1)} ${t.sessions.unitKwh}`;
  const value = (row: StaffUsage) => format.money([{ currency: row.currency ?? "XAF", decimals: row.decimals ?? 0, minor: row.valueMinor }])[0];

  return [
    {
      key: "member",
      header: labels.member,
      text: (row) => row.customerName,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}?tab=sessions`)} className="font-medium">
          {row.customerName}
        </CellLink>
      ),
    },
    { key: "sessions", header: labels.charges, align: "right", text: (row) => row.sessions, cell: (row) => format.number(row.sessions) },
    { key: "energy", header: labels.energy, align: "right", text: energy, cell: (row) => <span className="figure font-semibold">{energy(row)}</span> },
    { key: "value", header: labels.value, align: "right", text: value, cell: value },
    {
      key: "last",
      header: labels.last,
      text: (row) => (row.lastChargeAt ? format.dateTime(row.lastChargeAt) : ""),
      cell: (row) => (row.lastChargeAt ? format.dateTime(row.lastChargeAt) : t.common.none),
    },
  ];
}
