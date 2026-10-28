"use client";

import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { Icon } from "@/shared/ui/Icon";
import { useLogout } from "../hooks/useLogout";

export function LogoutControl() {
  const { t } = useI18n();
  const [open, setOpen] = useState(false);
  const logout = useLogout();

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label={t.shell.logout}
        title={t.shell.logout}
        className="inline-flex size-9 shrink-0 items-center justify-center rounded-control text-ink-muted hover:bg-ink-raised hover:text-white"
      >
        <Icon name="logout" />
      </button>
      <ConfirmDialog
        open={open}
        title={t.dialogs.logoutTitle}
        message={t.dialogs.logoutMessage}
        confirmLabel={t.shell.logout}
        cancelLabel={t.dialogs.cancel}
        loading={logout.isPending}
        onConfirm={() => logout.mutate(undefined, { onError: () => setOpen(false) })}
        onCancel={() => setOpen(false)}
      />
    </>
  );
}
