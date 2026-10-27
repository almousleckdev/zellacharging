"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { Button } from "@/shared/ui/Button";
import { EmptyState } from "@/shared/ui/EmptyState";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { Pagination } from "@/shared/ui/Pagination";
import { SearchInput } from "@/shared/ui/SearchInput";
import { Select } from "@/shared/ui/Select";
import { TableSkeleton } from "@/shared/ui/TableSkeleton";
import { USERS_PAGE_SIZE, useUsers } from "../api/useUsers";
import type { AdminUser } from "../schemas";
import { UserActionConfirm } from "./UserActionConfirm";
import type { UserAction } from "./UserActionConfirm";
import { UserFormDialog } from "./UserFormDialog";
import { UsersTable } from "./UsersTable";

type Dialog = { kind: "form"; user: AdminUser | null } | { kind: "action"; action: UserAction; user: AdminUser } | null;

const STATUSES = ["INVITED", "ACTIVE", "DISABLED"] as const;

export function UsersPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const { params, update } = useUrlParams();
  const [dialog, setDialog] = useState<Dialog>(null);
  const text = params.get("q") ?? "";
  const status = params.get("status") ?? "";
  const page = Math.max(0, Number(params.get("page")) || 0);
  const users = useUsers({ text, status, page });
  const data = users.data;
  const statuses: Record<string, string> = t.users.statuses;

  if (!profile.permissions.includes("ADMINS_MANAGE")) {
    return <EmptyState icon="shield" title={errorMessage(t, "PERMISSION_DENIED")} text="" />;
  }

  return (
    <div className="mx-auto flex max-w-6xl flex-col">
      <PageHeader
        title={t.users.title}
        subtitle={t.users.subtitle}
        trail={[t.shell.breadcrumbRoot, t.users.title]}
        actions={
          <div className="flex items-center gap-4">
            {data && <p className="text-sm text-muted">{interpolate(t.users.results, { count: format.number(data.total) })}</p>}
            <Button onClick={() => setDialog({ kind: "form", user: null })}>{t.users.invite}</Button>
          </div>
        }
      />
      <section className="overflow-hidden rounded-card border border-border bg-background">
        <div className="flex flex-col gap-3 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
          <SearchInput label={t.users.searchLabel} placeholder={t.users.searchPlaceholder} value={text} onSearch={(value) => update({ q: value })} />
          <div className="w-full sm:w-56">
            <Select
              label={t.users.statusLabel}
              hideLabel
              value={status}
              onChange={(event) => update({ status: event.target.value })}
              options={[{ value: "", label: t.users.allStatuses }, ...STATUSES.map((value) => ({ value, label: statuses[value] }))]}
            />
          </div>
        </div>
        {users.isPending && (
          <div role="status" aria-busy="true">
            <span className="sr-only">{t.common.loading}</span>
            <TableSkeleton columns={6} />
          </div>
        )}
        {users.isError && (
          <div className="p-5">
            <ErrorState message={t.users.loadError} onRetry={() => void users.refetch()} />
          </div>
        )}
        {data && data.items.length === 0 && (
          <div className="p-5">
            <EmptyState icon="users" title={t.users.emptyTitle} text={t.users.emptyText} />
          </div>
        )}
        {data && data.items.length > 0 && (
          <UsersTable
            items={data.items}
            onEdit={(user) => setDialog({ kind: "form", user })}
            onAction={(action, user) => setDialog({ kind: "action", action, user })}
          />
        )}
        <Pagination page={page} pageCount={data ? Math.ceil(data.total / USERS_PAGE_SIZE) : 0} onChange={(next) => update({ page: next === 0 ? null : String(next) }, false)} />
      </section>
      {dialog?.kind === "form" && <UserFormDialog key={dialog.user?.id ?? "new"} user={dialog.user} onClose={() => setDialog(null)} />}
      {dialog?.kind === "action" && <UserActionConfirm action={dialog.action} user={dialog.user} onClose={() => setDialog(null)} />}
    </div>
  );
}
