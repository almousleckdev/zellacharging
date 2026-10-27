"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { CustomerItem } from "../schemas";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

export function CustomersTable({ items, showCountry }: { items: CustomerItem[]; showCountry: boolean }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile, scopedHref } = useSession();
  const labels = t.customers.columns;
  const countryName = (code: string) => profile.countries.find((country) => country.code === code)?.name ?? code;

  const columns: Column<CustomerItem>[] = [
    {
      key: "customer",
      header: labels.customer,
      cell: (row) => (
        <Link href={scopedHref(`/customers/${row.country}/${row.id}`)} className="group flex flex-col">
          <span className="font-medium text-foreground group-hover:text-primary-text">
            {row.firstName} {row.lastName}
          </span>
          <span className="text-xs text-subtle">{row.email ?? "—"}</span>
        </Link>
      ),
    },
    { key: "phone", header: labels.phone, cell: (row) => row.phone ?? "—" },
    ...(showCountry ? [{ key: "country", header: labels.country, cell: (row: CustomerItem) => countryName(row.country) }] : []),
    { key: "status", header: labels.status, cell: (row) => <CustomerStatusBadge status={row.status} /> },
    { key: "joined", header: labels.joined, align: "right" as const, cell: (row) => format.date(row.createdAt) },
  ];

  return <DataTable caption={t.customers.title} columns={columns} rows={items} rowKey={(row) => `${row.country}-${row.id}`} />;
}
