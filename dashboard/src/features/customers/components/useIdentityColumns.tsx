"use client";

import { customerAvatarUrl, initialsOf } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Avatar } from "@/shared/ui/Avatar";
import { Badge } from "@/shared/ui/Badge";
import type { Column } from "@/shared/ui/DataTable";
import type { CustomerDetail } from "../schemas";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

function Contact({ value, verified }: { value: string | null; verified: boolean }) {
  const { t } = useI18n();
  if (!value) {
    return null;
  }
  return (
    <span className="flex flex-col items-start gap-1">
      <span>{value}</span>
      <Badge tone={verified ? "success" : "neutral"}>{verified ? t.customers.detail.verified : t.customers.detail.unverified}</Badge>
    </span>
  );
}

export function useIdentityColumns(): Column<CustomerDetail>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const labels = t.customers.detail;
  const countryName = (row: CustomerDetail) => profile.countries.find((item) => item.code === row.country)?.name ?? row.country;
  const name = (row: CustomerDetail) => `${row.firstName} ${row.lastName}`;

  return [
    {
      key: "customer",
      header: t.customers.columns.customer,
      text: name,
      cell: (row) => (
        <span className="flex items-center gap-3">
          <Avatar initials={initialsOf(row.firstName, row.lastName, row.email ?? "")} src={customerAvatarUrl(row.country, row.id, row.avatarVersion)} />
          <span className="font-medium text-foreground">{name(row)}</span>
        </span>
      ),
    },
    { key: "email", header: labels.email, text: (row) => row.email ?? "", cell: (row) => <Contact value={row.email} verified={row.emailVerified} /> },
    { key: "phone", header: labels.phone, text: (row) => row.phone ?? "", cell: (row) => <Contact value={row.phone} verified={row.phoneVerified} /> },
    { key: "country", header: labels.country, text: countryName, cell: countryName },
    { key: "status", header: labels.status, text: (row) => row.status, cell: (row) => <CustomerStatusBadge status={row.status} /> },
    { key: "joined", header: labels.joined, align: "right", text: (row) => format.date(row.createdAt), cell: (row) => format.date(row.createdAt) },
  ];
}
