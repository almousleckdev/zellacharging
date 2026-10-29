"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useLogout } from "../hooks/useLogout";

export function LogoutDialog({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { t } = useI18n();
  const logout = useLogout();

  return (
    <ConfirmDialog
      open={open}
      title={t.dialogs.logoutTitle}
      message={t.dialogs.logoutMessage}
      confirmLabel={t.shell.logout}
      cancelLabel={t.dialogs.cancel}
      loading={logout.isPending}
      onConfirm={() => logout.mutate(undefined, { onError: onClose })}
      onCancel={onClose}
    />
  );
}
