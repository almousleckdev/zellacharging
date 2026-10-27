"use client";

import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ConfirmDialog } from "@/shared/ui/ConfirmDialog";
import { useToast } from "@/shared/ui/ToastProvider";
import { displayName } from "@/features/session/avatarUrl";
import { useUserMutations } from "../api/useUserMutations";
import type { AdminUser } from "../schemas";

export type UserAction = "disable" | "enable" | "remove" | "resend";

type UserActionConfirmProps = {
  action: UserAction;
  user: AdminUser;
  onClose: () => void;
};

export function UserActionConfirm({ action, user, onClose }: UserActionConfirmProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const mutations = useUserMutations();
  const texts = t.users.confirms;
  const name = displayName(user.firstName, user.lastName, user.email);
  const mutation = mutations[action];
  const copy = {
    disable: { title: texts.disableTitle, message: texts.disableText, confirm: t.users.actions.disable },
    enable: { title: texts.enableTitle, message: texts.enableText, confirm: t.users.actions.enable },
    remove: { title: texts.removeTitle, message: texts.removeText, confirm: t.users.actions.remove },
    resend: { title: texts.resendTitle, message: texts.resendText, confirm: t.users.actions.resend },
  }[action];

  function confirm() {
    mutation.mutate(user.id, {
      onSuccess: (result) => {
        const sent = (result as { emailSent?: boolean } | null)?.emailSent;
        const done = {
          disable: t.users.toasts.disabled,
          enable: t.users.toasts.enabled,
          remove: t.users.toasts.removed,
          resend: sent === false ? t.users.toasts.resentNoEmail : t.users.toasts.resent,
        }[action];
        notify(sent === false ? "error" : "success", done);
        onClose();
      },
      onError: (error) => {
        notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN"));
        onClose();
      },
    });
  }

  return (
    <ConfirmDialog
      open
      title={copy.title}
      message={interpolate(copy.message, { name })}
      confirmLabel={copy.confirm}
      cancelLabel={t.dialogs.cancel}
      loading={mutation.isPending}
      onConfirm={confirm}
      onCancel={onClose}
    />
  );
}
