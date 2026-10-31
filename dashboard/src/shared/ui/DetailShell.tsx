"use client";

import Link from "next/link";
import type { ReactNode } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { ApiError } from "@/shared/api/adminFetch";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import { Icon } from "./Icon";
import type { IconName } from "./Icon";
import { PageHeader } from "./PageHeader";
import { Skeleton } from "./Skeleton";

type DetailShellProps = {
  backHref: string;
  backLabel: string;
  title: string;
  badge?: ReactNode;
  actions?: ReactNode;
  icon: IconName;
  query: { isPending: boolean; isError: boolean; error: unknown; refetch: () => unknown };
  notFound: string;
  loadError: string;
  children: ReactNode;
};

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

export function DetailShell({ backHref, backLabel, title, badge, actions, icon, query, notFound, loadError, children }: DetailShellProps) {
  const { scopedHref } = useSession();
  const { t } = useI18n();
  const missing = query.error instanceof ApiError && query.error.status === 404;

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-3">
        <Link href={scopedHref(backHref)} className="inline-flex items-center gap-1.5 self-start text-sm text-muted hover:text-foreground">
          <Icon name="arrowLeft" className="size-4" />
          {backLabel}
        </Link>
        <PageHeader
          title={title}
          actions={
            <div className="flex items-center gap-3">
              {badge}
              {actions}
            </div>
          }
        />
      </div>
      {query.isPending && (
        <>
          <span className="sr-only">{t.common.loading}</span>
          <DetailSkeleton />
        </>
      )}
      {missing && <EmptyState icon={icon} title={notFound} text="" />}
      {query.isError && !missing && <ErrorState message={loadError} onRetry={() => void query.refetch()} />}
      {!query.isPending && !query.isError && children}
    </div>
  );
}
