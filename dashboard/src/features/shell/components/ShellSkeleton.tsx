"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { PageSkeleton } from "@/shared/ui/PageSkeleton";
import { Skeleton } from "@/shared/ui/Skeleton";

function SidebarSkeleton() {
  return (
    <div aria-hidden="true" className="hidden w-64 shrink-0 flex-col gap-8 border-r border-sidebar-line bg-sidebar p-5 lg:flex [&_.skeleton]:bg-sidebar-raised">
      <Skeleton className="h-6 w-28" />
      <div className="flex flex-col gap-7">
        {[4, 3, 1].map((rows, group) => (
          <div key={group} className="flex flex-col gap-2.5">
            <Skeleton className="h-3 w-20" />
            {Array.from({ length: rows }, (_, row) => (
              <Skeleton key={row} className="h-9 w-full" />
            ))}
          </div>
        ))}
      </div>
      <div className="mt-auto flex items-center gap-3">
        <Skeleton className="size-9 rounded-full" />
        <div className="flex flex-1 flex-col gap-2">
          <Skeleton className="h-3 w-4/5" />
          <Skeleton className="h-3 w-1/2" />
        </div>
      </div>
    </div>
  );
}

export function ShellSkeleton() {
  const { t } = useI18n();

  return (
    <div role="status" aria-busy="true" className="flex min-h-screen bg-canvas">
      <span suppressHydrationWarning className="sr-only">
        {t.common.loading}
      </span>
      <SidebarSkeleton />
      <div className="flex min-w-0 flex-1 flex-col">
        <div aria-hidden="true" className="flex h-16 items-center justify-between border-b border-border bg-background px-4 sm:px-6">
          <Skeleton className="size-9 lg:hidden" />
          <Skeleton className="ml-auto h-9 w-44" />
        </div>
        <div className="flex-1 px-4 py-8 sm:px-6 lg:px-10">
          <PageSkeleton />
        </div>
      </div>
    </div>
  );
}
