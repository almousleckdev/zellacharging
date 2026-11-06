"use client";

import { useSearchParams } from "next/navigation";
import { useState } from "react";
import type { SubmitEvent } from "react";
import { passwordSetupSchema } from "@/features/onboarding/schemas";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { Alert } from "@/shared/ui/Alert";
import { AuthHeading } from "@/shared/ui/AuthHeading";
import { Button } from "@/shared/ui/Button";
import { PasswordField } from "@/shared/ui/PasswordField";
import { resetPassword } from "../api/authClient";
import { usePasswordRecovery } from "../hooks/usePasswordRecovery";
import { fieldErrorsOf } from "../validation";
import type { FieldErrors } from "../validation";
import { AuthNotice } from "./AuthNotice";

export function ResetPasswordForm() {
  const { t } = useI18n();
  const labels = t.recovery;
  const token = useSearchParams().get("token");
  const recovery = usePasswordRecovery();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errors, setErrors] = useState<FieldErrors<"password" | "confirmPassword">>({});

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const parsed = passwordSetupSchema.safeParse({ password, confirmPassword });
    if (!parsed.success) {
      setErrors(fieldErrorsOf(parsed.error));
      return;
    }
    setErrors({});
    void recovery.run(() => resetPassword(token ?? "", parsed.data.password));
  }

  if (!token || recovery.errorCode === "RESET_LINK_INVALID") {
    return (
      <AuthNotice title={labels.invalidTitle} text={labels.invalidText} linkHref="/forgot-password" linkLabel={labels.requestNew} />
    );
  }
  if (recovery.done) {
    return <AuthNotice title={labels.doneTitle} text={labels.doneText} linkHref="/login" linkLabel={labels.backToLogin} />;
  }

  return (
    <div className="flex flex-col gap-6">
      <AuthHeading title={labels.resetTitle} subtitle={labels.resetSubtitle} />
      {recovery.errorCode && <Alert>{errorMessage(t, recovery.errorCode)}</Alert>}
      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-5">
        <PasswordField
          label={labels.password}
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={errors.password ? t.validation[errors.password] : undefined}
          hint={labels.passwordHint}
          disabled={recovery.pending}
          autoFocus
        />
        <PasswordField
          label={labels.confirm}
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          error={errors.confirmPassword ? t.validation[errors.confirmPassword] : undefined}
          disabled={recovery.pending}
        />
        <Button type="submit" fullWidth loading={recovery.pending}>
          {recovery.pending ? labels.resetting : labels.reset}
        </Button>
      </form>
    </div>
  );
}
