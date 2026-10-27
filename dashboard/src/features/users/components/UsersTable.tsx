"use client";

import { displayName, initialsOf, userAvatarUrl } from "@/features/session/avatarUrl";
import { useSession } from "@/features/session/SessionProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ActionMenu } from "@/shared/ui/ActionMenu";
import type { ActionItem } from "@/shared/ui/ActionMenu";
import { Avatar } from "@/shared/ui/Avatar";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { AdminUser } from "../schemas";
import type { UserAction } from "./UserActionConfirm";
import { UserStatusBadge } from "./UserStatusBadge";

type UsersTableProps = {
  items: AdminUser[];
  onEdit: (user: AdminUser) => void;
  onAction: (action: UserAction, user: AdminUser) => void;
};

export function UsersTable({ items, onEdit, onAction }: UsersTableProps) {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const labels = t.users.columns;
  const roles: Record<string, string> = t.shell.roles;
  const countryName = (code: string) => profile.countries.find((country) => country.code === code)?.name ?? code;

  function actionsOf(user: AdminUser): ActionItem[] {
    if (user.role === "SUPER_ADMIN") {
      return [];
    }
    const items: ActionItem[] = [{ label: t.users.actions.edit, onSelect: () => onEdit(user) }];
    if (user.status === "INVITED") {
      items.push({ label: t.users.actions.resend, onSelect: () => onAction("resend", user) });
      items.push({ label: t.users.actions.remove, danger: true, onSelect: () => onAction("remove", user) });
    } else if (user.status === "ACTIVE") {
      items.push({ label: t.users.actions.disable, danger: true, onSelect: () => onAction("disable", user) });
    } else {
      items.push({ label: t.users.actions.enable, onSelect: () => onAction("enable", user) });
    }
    return items;
  }

  const columns: Column<AdminUser>[] = [
    {
      key: "user",
      header: labels.user,
      cell: (user) => (
        <div className="flex items-center gap-3">
          <Avatar initials={initialsOf(user.firstName, user.lastName, user.email)} src={userAvatarUrl(user.id, user.avatarVersion)} />
          <div className="flex min-w-0 flex-col">
            <span className="truncate font-medium text-foreground">
              {user.firstName || user.lastName ? displayName(user.firstName, user.lastName, user.email) : t.users.pendingName}
            </span>
            <span className="truncate text-xs text-subtle">{user.email}</span>
          </div>
        </div>
      ),
    },
    { key: "role", header: labels.role, cell: (user) => roles[user.role] ?? user.role },
    { key: "countries", header: labels.countries, cell: (user) => (user.role === "SUPER_ADMIN" ? t.shell.country.all : user.countries.map(countryName).join(", ")) },
    { key: "status", header: labels.status, cell: (user) => <UserStatusBadge status={user.status} /> },
    { key: "login", header: labels.lastLogin, cell: (user) => (user.lastLoginAt ? format.dateTime(user.lastLoginAt) : t.users.never) },
    {
      key: "actions",
      header: "",
      align: "right",
      cell: (user) => {
        const items = actionsOf(user);
        const name = displayName(user.firstName, user.lastName, user.email);
        return items.length > 0 ? <ActionMenu label={interpolate(t.users.actions.menu, { name })} items={items} /> : null;
      },
    },
  ];

  return <DataTable caption={t.users.title} columns={columns} rows={items} rowKey={(user) => user.id} />;
}
