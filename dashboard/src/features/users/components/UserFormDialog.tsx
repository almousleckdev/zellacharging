"use client";

import { useState } from "react";
import { fieldErrorsOf } from "@/features/auth/validation";
import type { FieldErrors } from "@/features/auth/validation";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { Modal } from "@/shared/ui/Modal";
import { TextField } from "@/shared/ui/TextField";
import { useToast } from "@/shared/ui/ToastProvider";
import { useUserMutations } from "../api/useUserMutations";
import { accessFormSchema } from "../formSchema";
import { ROLE_PRESETS } from "../schemas";
import type { AccessDefinition, AdminUser, AssignableRole } from "../schemas";
import { AccessFields } from "./AccessFields";

type UserFormDialogProps = {
  user: AdminUser | null;
  onClose: () => void;
};

function initialAccess(user: AdminUser | null): AccessDefinition {
  const role = (user?.role ?? "ADMIN") as AssignableRole;
  return { role, permissions: user ? user.permissions : [...ROLE_PRESETS[role]], countries: user?.countries ?? [] };
}

export function UserFormDialog({ user, onClose }: UserFormDialogProps) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { invite, update } = useUserMutations();
  const [email, setEmail] = useState(user?.email ?? "");
  const [access, setAccess] = useState(initialAccess(user));
  const [errors, setErrors] = useState<FieldErrors<"email" | "countries">>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const labels = t.users.form;
  const pending = invite.isPending || update.isPending;

  function handleError(error: unknown) {
    setServerError(error instanceof ApiError ? errorMessage(t, error.code) : errorMessage(t, "UNKNOWN"));
  }

  function submit() {
    const parsed = accessFormSchema.safeParse({ email: user ? "valid@zella.test" : email, countries: access.countries });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    setServerError(null);
    if (user) {
      update.mutate({ id: user.id, ...access }, {
        onSuccess: () => {
          notify("success", t.users.toasts.updated);
          onClose();
        },
        onError: handleError,
      });
      return;
    }
    invite.mutate({ email: parsed.data.email, ...access }, {
      onSuccess: (result) => {
        notify(result.emailSent ? "success" : "error", result.emailSent ? interpolate(t.users.toasts.invited, { name: result.admin.email }) : t.users.toasts.invitedNoEmail);
        onClose();
      },
      onError: handleError,
    });
  }

  return (
    <Modal
      open
      size="lg"
      busy={pending}
      title={user ? labels.editTitle : labels.createTitle}
      description={user ? user.email : undefined}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={pending}>
            {labels.cancel}
          </Button>
          <Button onClick={submit} loading={pending}>
            {pending ? labels.submitting : user ? labels.submitEdit : labels.submitCreate}
          </Button>
        </>
      }
    >
      <form onSubmit={(event) => { event.preventDefault(); submit(); }} noValidate className="flex flex-col gap-6">
        {serverError && <Alert>{serverError}</Alert>}
        {!user && (
          <TextField
            label={labels.email}
            type="email"
            autoComplete="off"
            value={email}
            hint={labels.emailHint}
            onChange={(event) => setEmail(event.target.value)}
            error={errors.email ? t.validation[errors.email] : undefined}
            disabled={pending}
            autoFocus
          />
        )}
        <AccessFields
          value={access}
          onChange={setAccess}
          disabled={pending}
          countriesError={errors.countries ? t.validation[errors.countries] : undefined}
        />
      </form>
    </Modal>
  );
}
