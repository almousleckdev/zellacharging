"use client";

import { customerAvatarUrl, initialsOf } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Avatar } from "@/shared/ui/Avatar";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import type { CustomerItem } from "../schemas";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

export function useCustomerColumns(): Column<CustomerItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const labels = t.customers.columns;
  const statuses: Record<string, string> = t.customers.statuses;
  const showCountry = profile.countries.length > 1 && country === "ALL";
  const countryName = (code: string) => profile.countries.find((item) => item.code === code)?.name ?? code;
  const name = (row: CustomerItem) => `${row.firstName} ${row.lastName}`;

  const money = (row: CustomerItem, minor: number | null | undefined) =>
    minor === null || minor === undefined || !row.currency
      ? t.common.none
      : format.money([{ currency: row.currency, decimals: row.decimals ?? 0, minor }])[0];
  const last = (row: CustomerItem) => (row.lastSessionAt ? format.date(row.lastSessionAt) : t.common.none);

  return [
    {
      key: "customer",
      header: labels.customer,
      sortKey: "name",
      text: (row) => `${name(row)} <${row.email ?? ""}> ${row.phone ?? ""}`,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.id}`)} className="flex items-center gap-3">
          <Avatar initials={initialsOf(row.firstName, row.lastName, row.email ?? "")} src={customerAvatarUrl(row.country, row.id, row.avatarVersion)} />
          <span className="flex flex-col">
            <span className="font-medium text-foreground">{name(row)}</span>
            <span className="text-xs text-subtle">{[row.email, row.phone].filter(Boolean).join(" · ")}</span>
          </span>
        </CellLink>
      ),
    },
    ...(showCountry
      ? [{ key: "country", header: labels.country, text: (row: CustomerItem) => countryName(row.country), cell: (row: CustomerItem) => countryName(row.country) }]
      : []),
    { key: "status", header: labels.status, text: (row) => statuses[row.status] ?? row.status, cell: (row) => <CustomerStatusBadge status={row.status} /> },
    { key: "charges", header: labels.charges, align: "right", text: (row) => row.completedSessions ?? 0, cell: (row) => format.number(row.completedSessions ?? 0) },
    {
      key: "spent",
      header: labels.spent,
      align: "right",
      text: (row) => money(row, row.spentMinor),
      cell: (row) => <span className="figure font-semibold">{money(row, row.spentMinor)}</span>,
    },
    { key: "last", header: labels.lastCharge, align: "right", text: last, cell: last },
    { key: "joined", header: labels.joined, align: "right", sortKey: "date", text: (row) => format.date(row.createdAt), cell: (row) => format.date(row.createdAt) },
  ];
}
