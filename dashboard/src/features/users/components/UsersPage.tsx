"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { FilterSelect } from "@/shared/lists/FilterSelect";
import { ListToolbar } from "@/shared/lists/ListToolbar";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { Button } from "@/shared/ui/Button";
import { EmptyState } from "@/shared/ui/EmptyState";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";
import { SearchInput } from "@/shared/ui/SearchInput";
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
    <div className="flex flex-col gap-6">
      <PageHeader
        title={t.users.title}
        subtitle={t.users.subtitle}
        actions={
          <div className="flex items-center gap-4">
            {data && <p className="text-sm text-muted">{interpolate(t.users.results, { count: format.number(data.total) })}</p>}
            <Button onClick={() => setDialog({ kind: "form", user: null })}>{t.users.invite}</Button>
          </div>
        }
      />
      <ListPanel
        toolbar={
          <ListToolbar
            search={<SearchInput label={t.users.searchLabel} placeholder={t.users.searchPlaceholder} value={text} onSearch={(value) => update({ q: value })} />}
            filters={
              <FilterSelect
                config={{
                  key: "status",
                  label: t.users.statusLabel,
                  allLabel: t.users.allStatuses,
                  options: STATUSES.map((value) => ({ value, label: statuses[value] })),
                }}
                value={status}
                onChange={(value) => update({ status: value })}
              />
            }
          />
        }
        pending={users.isPending}
        failed={users.isError}
        onRetry={() => void users.refetch()}
        loadError={t.users.loadError}
        empty={{ icon: "users", title: t.users.emptyTitle, text: t.users.emptyText }}
        isEmpty={data?.items.length === 0}
        page={page}
        size={USERS_PAGE_SIZE}
        total={data?.total ?? 0}
        onPage={(next) => update({ page: next === 0 ? null : String(next) }, false)}
      >
        {data && data.items.length > 0 && (
          <UsersTable
            items={data.items}
            onEdit={(user) => setDialog({ kind: "form", user })}
            onAction={(action, user) => setDialog({ kind: "action", action, user })}
          />
        )}
      </ListPanel>
      {dialog?.kind === "form" && <UserFormDialog key={dialog.user?.id ?? "new"} user={dialog.user} onClose={() => setDialog(null)} />}
      {dialog?.kind === "action" && <UserActionConfirm action={dialog.action} user={dialog.user} onClose={() => setDialog(null)} />}
    </div>
  );
}
