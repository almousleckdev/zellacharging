"use client";

import { useSession } from "@/features/session/SessionProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { EmptyState } from "@/shared/ui/EmptyState";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Pagination } from "@/shared/ui/Pagination";
import { TableSkeleton } from "@/shared/ui/TableSkeleton";
import { PAGE_SIZE, useCustomers } from "../api/useCustomers";
import { CustomersTable } from "./CustomersTable";
import { CustomersToolbar } from "./CustomersToolbar";

export function CustomersPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile } = useSession();
  const { params, update } = useUrlParams();
  const text = params.get("q") ?? "";
  const status = params.get("status") ?? "";
  const page = Math.max(0, Number(params.get("page")) || 0);
  const customers = useCustomers({ country, text, status, page });
  const data = customers.data;
  const pageCount = data ? Math.ceil(data.total / PAGE_SIZE) : 0;

  return (
    <div className="mx-auto flex max-w-6xl flex-col">
      <PageHeader
        title={t.customers.title}
        subtitle={t.customers.subtitle}
        trail={[t.shell.breadcrumbRoot, t.customers.title]}
        actions={data && <p className="text-sm text-muted">{interpolate(t.customers.results, { count: format.number(data.total) })}</p>}
      />
      <section className="overflow-hidden rounded-card border border-border bg-background">
        <CustomersToolbar text={text} status={status} onText={(value) => update({ q: value })} onStatus={(value) => update({ status: value })} />
        {customers.isPending && (
          <div role="status" aria-busy="true">
            <span className="sr-only">{t.common.loading}</span>
            <TableSkeleton />
          </div>
        )}
        {customers.isError && (
          <div className="p-5">
            <ErrorState message={t.customers.loadError} onRetry={() => void customers.refetch()} />
          </div>
        )}
        {data && data.items.length === 0 && (
          <div className="p-5">
            <EmptyState icon="users" title={t.customers.emptyTitle} text={t.customers.emptyText} />
          </div>
        )}
        {data && data.items.length > 0 && (
          <CustomersTable items={data.items} showCountry={profile.countries.length > 1 && country === "ALL"} />
        )}
        <Pagination page={page} pageCount={pageCount} onChange={(next) => update({ page: next === 0 ? null : String(next) }, false)} />
      </section>
    </div>
  );
}
