"use client";

import { useState } from "react";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";
import type { CustomerDetail } from "../schemas";
import { EditNameDialog } from "./EditNameDialog";
import { SetPasswordDialog } from "./SetPasswordDialog";
import { useIdentityColumns } from "./useIdentityColumns";

export function IdentityCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const { profile } = useSession();
  const columns = useIdentityColumns();
  const [editing, setEditing] = useState(false);
  const [settingPassword, setSettingPassword] = useState(false);
  const canEdit = profile.role === "SUPER_ADMIN";
  const canSetPassword = (canEdit || profile.role === "ADMIN") && profile.permissions.includes("CUSTOMERS_MANAGE");

  return (
    <Card
      title={t.customers.detail.identity}
      flush
      action={(canEdit || canSetPassword) && (
        <span className="flex flex-wrap gap-2">
          {canSetPassword && <Button variant="secondary" onClick={() => setSettingPassword(true)}>{t.customers.support.password.button}</Button>}
          {canEdit && <Button variant="secondary" onClick={() => setEditing(true)}>{t.customers.support.name.button}</Button>}
        </span>
      )}
    >
      <DataTable caption={t.customers.detail.identity} columns={columns} rows={[customer]} rowKey={(row) => row.id} />
      {editing && <EditNameDialog customer={customer} onClose={() => setEditing(false)} />}
      {settingPassword && <SetPasswordDialog customer={customer} onClose={() => setSettingPassword(false)} />}
    </Card>
  );
}
