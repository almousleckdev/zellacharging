"use client";

import type { ReactNode } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import type { IconName } from "./Icon";
import { Pagination } from "./Pagination";
import { TableSkeleton } from "./TableSkeleton";

type ListPanelProps = {
  toolbar?: ReactNode;
  pending: boolean;
  failed: boolean;
  onRetry: () => void;
  loadError: string;
  empty: { icon: IconName; title: string; text: string };
  isEmpty: boolean;
  page?: number;
  size?: number;
  total?: number;
  onPage?: (page: number) => void;
  onSize?: (size: number) => void;
  children: ReactNode;
};

export function ListPanel({
  toolbar,
  pending,
  failed,
  onRetry,
  loadError,
  empty,
  isEmpty,
  page = 0,
  size = 20,
  total = 0,
  onPage,
  onSize,
  children,
}: ListPanelProps) {
  const { t } = useI18n();

  return (
    <section className="overflow-hidden rounded-card border border-border bg-background shadow-card">
      {toolbar}
      {pending && (
        <div role="status" aria-busy="true">
          <span className="sr-only">{t.common.loading}</span>
          <TableSkeleton />
        </div>
      )}
      {failed && (
        <div className="p-5">
          <ErrorState message={loadError} onRetry={onRetry} />
        </div>
      )}
      {isEmpty && (
        <div className="p-5">
          <EmptyState icon={empty.icon} title={empty.title} text={empty.text} />
        </div>
      )}
      {children}
      {onPage && <Pagination page={page} size={size} total={total} onPage={onPage} onSize={onSize} />}
    </section>
  );
}
