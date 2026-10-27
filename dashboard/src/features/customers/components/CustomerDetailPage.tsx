"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { EmptyState } from "@/shared/ui/EmptyState";
import { ErrorState } from "@/shared/ui/ErrorState";
import { Icon } from "@/shared/ui/Icon";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Skeleton } from "@/shared/ui/Skeleton";
import { useCustomer } from "../api/useCustomer";
import { ActivityCard } from "./ActivityCard";
import { IdentityCard } from "./IdentityCard";
import { WalletCard } from "./WalletCard";

function DetailSkeleton() {
  return (
    <div role="status" aria-busy="true" className="grid gap-6 lg:grid-cols-3">
      {Array.from({ length: 3 }, (_, index) => (
        <div key={index} className="flex flex-col gap-4 rounded-card border border-border bg-background p-5">
          <Skeleton className="h-4 w-32" />
          {Array.from({ length: 5 }, (_, row) => (
            <Skeleton key={row} className="h-3.5" />
          ))}
        </div>
      ))}
    </div>
  );
}

export function CustomerDetailPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const params = useParams<{ country: string; id: string }>();
  const customer = useCustomer(params.country, params.id);
  const labels = t.customers.detail;
  const data = customer.data;
  const missing = customer.error instanceof ApiError && customer.error.status === 404;
  const name = data ? `${data.firstName} ${data.lastName}` : t.customers.title;

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-2">
      <Link href={scopedHref("/customers")} className="inline-flex items-center gap-1.5 text-sm text-muted hover:text-foreground">
        <Icon name="arrowLeft" className="size-4" />
        {labels.back}
      </Link>
      <PageHeader title={name} trail={[t.shell.breadcrumbRoot, t.customers.title, name]} />
      {customer.isPending && <DetailSkeleton />}
      {missing && <EmptyState icon="users" title={labels.notFound} text="" />}
      {customer.isError && !missing && <ErrorState message={labels.loadError} onRetry={() => void customer.refetch()} />}
      {data && (
        <div className="grid items-start gap-6 lg:grid-cols-3">
          <IdentityCard customer={data} />
          <WalletCard wallet={data.wallet} />
          <ActivityCard sessions={data.sessions} currency={data.wallet?.currency ?? "XAF"} decimals={data.wallet?.decimals ?? 0} />
        </div>
      )}
    </div>
  );
}
