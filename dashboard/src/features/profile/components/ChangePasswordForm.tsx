"use client";

import { useEffect, useState } from "react";
import type { SubmitEvent } from "react";
import { useLogout } from "@/features/shell/hooks/useLogout";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Alert } from "@/shared/ui/Alert";
import { Button } from "@/shared/ui/Button";
import { PasswordField } from "@/shared/ui/PasswordField";
import { fieldErrorsOf } from "@/features/auth/validation";
import type { FieldErrors } from "@/features/auth/validation";
import { useChangePassword } from "../hooks/useChangePassword";
import { passwordChangeSchema } from "../schemas";

type Field = "currentPassword" | "newPassword" | "confirmPassword";

const SIGN_OUT_DELAY_MS = 2500;
const EMPTY = { currentPassword: "", newPassword: "", confirmPassword: "" };

export function ChangePasswordForm() {
  const { t } = useI18n();
  const change = useChangePassword();
  const logout = useLogout();
  const [values, setValues] = useState(EMPTY);
  const [errors, setErrors] = useState<FieldErrors<Field>>({});
  const done = change.isSuccess;

  useEffect(() => {
    if (!done) {
      return;
    }
    const timer = window.setTimeout(() => logout.mutate(), SIGN_OUT_DELAY_MS);
    return () => window.clearTimeout(timer);
  }, [done, logout]);

  function update(field: Field, value: string) {
    setValues((current) => ({ ...current, [field]: value }));
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = passwordChangeSchema.safeParse(values);
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    change.mutate(parsed.data, { onSuccess: () => setValues(EMPTY) });
  }

  const labels = t.profile.password;
  const message = (field: Field) => (errors[field] ? t.validation[errors[field]] : undefined);

  return (
    <form onSubmit={handleSubmit} noValidate className="flex max-w-md flex-col gap-4">
      <p className="text-sm text-muted">{labels.hint}</p>
      {change.isError && change.error instanceof ApiError && <Alert>{errorMessage(t, change.error.code)}</Alert>}
      {done && (
        <p role="status" className="rounded-control border border-border bg-success-soft px-3 py-2.5 text-sm text-success">
          {labels.success}
        </p>
      )}
      <PasswordField
        label={labels.current}
        autoComplete="current-password"
        value={values.currentPassword}
        onChange={(event) => update("currentPassword", event.target.value)}
        error={message("currentPassword")}
        disabled={change.isPending || done}
      />
      <PasswordField
        label={labels.next}
        autoComplete="new-password"
        value={values.newPassword}
        onChange={(event) => update("newPassword", event.target.value)}
        error={message("newPassword")}
        disabled={change.isPending || done}
      />
      <PasswordField
        label={labels.confirm}
        autoComplete="new-password"
        value={values.confirmPassword}
        onChange={(event) => update("confirmPassword", event.target.value)}
        error={message("confirmPassword")}
        disabled={change.isPending || done}
      />
      <div>
        <Button type="submit" loading={change.isPending} disabled={done}>
          {change.isPending ? labels.submitting : labels.submit}
        </Button>
      </div>
    </form>
  );
}
