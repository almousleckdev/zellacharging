"use client";

import { useState } from "react";
import type { SubmitEvent } from "react";
import { fieldErrorsOf } from "@/features/auth/validation";
import type { FieldErrors } from "@/features/auth/validation";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";
import { PasswordField } from "@/shared/ui/PasswordField";
import { passwordSetupSchema } from "../schemas";

type Props = {
  pending: boolean;
  onSubmit: (password: string) => void;
};

export function PasswordSetupStep({ pending, onSubmit }: Props) {
  const { t } = useI18n();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errors, setErrors] = useState<FieldErrors<"password" | "confirmPassword">>({});
  const labels = t.onboarding;

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = passwordSetupSchema.safeParse({ password, confirmPassword });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    onSubmit(parsed.data.password);
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
      <PasswordField label={labels.password} autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} error={errors.password ? t.validation[errors.password] : undefined} hint={labels.passwordHint} disabled={pending} autoFocus />
      <PasswordField label={labels.confirm} autoComplete="new-password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} error={errors.confirmPassword ? t.validation[errors.confirmPassword] : undefined} disabled={pending} />
      <Button type="submit" fullWidth loading={pending}>
        {pending ? labels.submitting : labels.submit}
      </Button>
    </form>
  );
}
